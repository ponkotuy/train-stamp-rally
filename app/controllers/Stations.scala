package controllers

import javax.inject.{Inject, Singleton}

import actors.{AttrRequest, ImageRequest, WikipediaActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import authes.Authenticator
import authes.Role.{Administrator, NormalUser}
import caches.LineStationsCache
import com.github.tototoshi.play2.json4s.Json4s
import models._
import modules.LocationSetterThread
import org.json4s._
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.InjectedController
import queries.CreateStationImpl
import scalikejdbc._
import utils.{FutureUtil, Wikipedia}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Stations @Inject() (json4s: Json4s, ws: WSClient, ec: ExecutionContext, system: ActorSystem, config: Configuration)
    extends InjectedController with Authenticator {
  import FutureUtil._
  import Responses._
  import json4s._
  import json4s.implicits._

  implicit val _ec: ExecutionContext = ec
  implicit val formats: Formats = DefaultFormats + StationRankSerializer
  implicit val timeout: Timeout = 10.seconds

  lazy val wiki = system.actorOf(Props(new WikipediaActor(new Wikipedia(ws))))

  def list(q: Option[String], limit: Option[Int]) = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      import models.DefaultAliases.s
      val where = q.filter(_.nonEmpty).map { name => sqls.like(s.name, s"%${name}").or.like(s.name, s"${name}%") }.getOrElse(sqls"true")
      val result = Station.findAllByWithLimitOffset(where, limit = limit.getOrElse(Int.MaxValue), orderings = Seq(s.id))
      Ok(Extraction.decompose(result))
    }
  }

  def update(stationId: Long) = Action(json) { implicit req =>
    withAuth(Administrator) { _ =>
      req.body.extractOpt[CreateStationImpl].fold(JSONParseError) { station =>
        station.station.fold(notFound(s"rankValue: ${station.rankValue}")) { s =>
          DB localTx { implicit session =>
            if (s.copy(id = stationId).update() == 0) notFound(s"station id=${stationId}")
            else Success
          }
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

  def image(stationId: Long) = Action.async { implicit req =>
    withAuthAsync(NormalUser) { _ =>
      val stImage: Future[StationImage] = StationImage.findById(stationId).fold {
        (wiki ? ImageRequest(stationId)).mapTo[Option[StationImage]].flatMap(fromOption)
      }(Future.successful)
      stImage.map { image =>
        image.image.fold(notFound("image")) { img =>
          Ok(img.bytes).as("image/jpeg")
        }
      }
    }
  }

  def attribution(stationId: Long) = Action.async { implicit req =>
    withAuthAsync(NormalUser) { _ =>
      if (StationImage.findById(stationId).map(_.imageId).contains(None)) Future.successful(notFound("attribution"))
      else {
        val attr = ImageAttribute.findById(stationId).fold {
          (wiki ? AttrRequest(stationId)).mapTo[Option[Attr]].flatMap(fromOption)
        }(Future.successful)
        attr.map { a => Ok(a.attribution).as(HTML) }
      }
    }
  }

  def lineStationList() = Action { implicit req =>
    withAuth(NormalUser) { _ => Ok(LineStationsCache()) }
  }

  def lineStation(lineStationId: Long) = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      Ok(Extraction.decompose(LineStation.joins(LineStation.stationRef).findById(lineStationId)))
    }
  }

  def replace(lineStationId: Long) = Action(json) { implicit req =>
    import scalikejdbc.TxBoundary.Either._
    withAuth(Administrator) { _ =>
      req.body.extractOpt[CreateStationImpl].fold(JSONParseError) { station =>
        station.station.fold(notFound(s"rankValue: ${station.rankValue}")) { s =>
          val result = DB localTx { implicit session =>
            val id = s.save()
            if (LineStation.updateById(lineStationId).withAttributes('stationId -> id) == 0)
              Left(notFound(s"lineStation id=${lineStationId}"))
            else Right(Success)
          }
          result.merge
        }
      }
    }
  }

  def restartGeo() = Action { implicit req =>
    withAuth(Administrator) { _ =>
      new LocationSetterThread(config).run()
      Success
    }
  }
}
