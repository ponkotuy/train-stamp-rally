package controllers

import actors.{AttrRequest, ImageRequest, WikipediaActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import authes.AuthConfigImpl
import authes.Role.{Administrator, NormalUser}
import caches.LineStationsCache
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models._
import modules.LocationSetterThread
import org.json4s._
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, Result}
import queries.CreateStationImpl
import scalikejdbc._
import utils.{FutureUtil, Wikipedia}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class Stations @Inject() (json4s: Json4s, ws: WSClient, ec: ExecutionContext, system: ActorSystem, config: Configuration)
    extends Controller with AuthElement with AuthConfigImpl {
  import FutureUtil._
  import Responses._
  import json4s._

  implicit val _ec: ExecutionContext = ec
  implicit val formats: Formats = DefaultFormats + StationRankSerializer
  implicit val timeout: Timeout = 10.seconds

  lazy val wiki = system.actorOf(Props(new WikipediaActor(new Wikipedia(ws))))

  def list(q: Option[String], limit: Option[Int]) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.DefaultAliases.s
    val where = q.filter(_.nonEmpty).map { name => sqls.like(s.name, s"%${name}").or.like(s.name, s"${name}%") }.getOrElse(sqls"true")
    val result = Station.findAllByWithLimitOffset(where, limit = limit.getOrElse(Int.MaxValue), orderings = Seq(s.id))
    Ok(Extraction.decompose(result))
  }

  def update(stationId: Long) = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateStationImpl].fold(JSONParseError) { station =>
      station.station.fold(notFound(s"rankValue: ${station.rankValue}")) { s =>
        DB localTx { implicit session =>
          if (s.copy(id = stationId).update() == 0) notFound(s"station id=${stationId}")
          else Success
        }
      }
    }
  }

  def show(stationId: Long) = Action {
    Ok(Extraction.decompose(Station.findById(stationId)))
  }

  def lines(stationId: Long) = Action {
    import models.DefaultAliases.ls
    val lineStations = LineStation.joins(LineStation.lineRef).findAllBy(sqls.eq(ls.stationId, stationId))
    Ok(Extraction.decompose(lineStations))
  }

  def image(stationId: Long) = AsyncStack(AuthorityKey -> NormalUser) { implicit req =>
    val stImage: Future[StationImage] = StationImage.findById(stationId).fold {
      (wiki ? ImageRequest(stationId)).mapTo[Option[StationImage]].flatMap(fromOption)
    }(Future.successful)
    stImage.map { image =>
      image.image.fold(notFound("image")) { img =>
        Ok(img.bytes).as("image/jpeg")
      }
    }
  }

  def attribution(stationId: Long) = AsyncStack(AuthorityKey -> NormalUser) { implicit req =>
    if (StationImage.findById(stationId).map(_.imageId).contains(None)) Future.successful(notFound("attribution"))
    else {
      val attr = ImageAttribute.findById(stationId).fold {
        (wiki ? AttrRequest(stationId)).mapTo[Option[Attr]].flatMap(fromOption)
      }(Future.successful)
      attr.map { a => Ok(a.attribution).as(HTML) }
    }
  }

  def lineStationList() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(LineStationsCache())
  }

  def lineStation(lineStationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(LineStation.joins(LineStation.stationRef).findById(lineStationId)))
  }

  def replace(lineStationId: Long) = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    import scalikejdbc.TxBoundary.Either._
    req.body.extractOpt[CreateStationImpl].fold(JSONParseError) { station =>
      station.station.fold(notFound(s"rankValue: ${station.rankValue}")) { s =>
        val result: Either[Result, Result] = DB localTx { implicit session =>
          val id = s.save()
          if (LineStation.updateById(lineStationId).withAttributes('stationId -> id) == 0)
            Left(notFound(s"lineStation id=${lineStationId}"))
          else Right(Success)
        }
        result.merge
      }
    }
  }

  def restartGeo() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    new LocationSetterThread(config).run()
    Success
  }
}
