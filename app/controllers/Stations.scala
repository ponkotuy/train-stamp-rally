package controllers

import authes.AuthConfigImpl
import authes.Role.{Administrator, NormalUser}
import caches.LineStationsCache
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{LineStation, Station, StationRankSerializer}
import net.liftweb.util.Html5
import org.json4s._
import org.json4s.native.JsonMethods
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import queries.CreateStationImpl
import scalikejdbc._
import utils.{ImgAttr, ImgSrc, Wikipedia}

import scala.concurrent.{ExecutionContext, Future}

class Stations @Inject()(json4s: Json4s, ws: WSClient, ec: ExecutionContext) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import Stations._
  import json4s._
  implicit val _ec: ExecutionContext = ec

  implicit lazy val wiki = new Wikipedia(ws)

  def list(q: Option[String]) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.DefaultAliases.s
    val where = q.map { name => sqls.like(s.name, s"%${name}").or.like(s.name , s"${name}%") }.getOrElse(sqls"true")
    Ok(Extraction.decompose(Station.findAllBy(where, Seq(s.id))))
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

  def show(stationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(Station.findById(stationId)))
  }

  def lines(stationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.DefaultAliases.ls
    val lineStations = LineStation.joins(LineStation.lineRef).findAllBy(sqls.eq(ls.stationId, stationId))
    Ok(Extraction.decompose(lineStations))
  }

  def image(stationId: Long) = AsyncStack(AuthorityKey -> NormalUser) { implicit req =>
    Station.findById(stationId).fold(Future.successful(notFound(s"station id = ${stationId}"))) { st =>
      for {
        imgSrc <- getImgSrc(st.name)
        res <- ws.url(imgSrc.withScheme).get()
      } yield {
        Ok(res.bodyAsBytes).as("image/jpeg")
      }
    }
  }

  def attribution(stationId: Long) = AsyncStack(AuthorityKey -> NormalUser) { implicit req =>
    Station.findById(stationId).fold(Future.successful(notFound(s"station id = ${stationId}"))) { st =>
      for {
        imgSrc <- getImgSrc(st.name)
        fName <- Future { imgSrc.origin.get }
        attr <- getAttribution(fName)
      } yield Ok(attr.attribution).as(HTML)
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
        val result = DB localTx { implicit session =>
          val id = s.save()
          if(LineStation.updateById(lineStationId).withAttributes('stationId -> id) == 0)
            Left(notFound(s"lineStation id=${lineStationId}"))
          else Right(Success)
        }
        result.merge
      }
    }
  }
}

object Stations {
  implicit val formats: Formats = DefaultFormats + StationRankSerializer

  class NotFoundImgSrc extends Exception("Not found imgsrc")
  def getImgSrc(stName: String)(implicit wiki: Wikipedia, ec: ExecutionContext): Future[ImgSrc] = {
    wiki.getHTML(stName + "駅").flatMap { content =>
      val res = for {
        html <- Html5.parse(content.body)
        elem <- (html \\ "img").find { img => 200 <= (img \ "@width").text.toInt }
      } yield {
        val srcset = (elem \ "@srcset").text
        val imgs = Wikipedia.parseSrcSet(srcset)
        imgs.maxBy(_.scale)
      }
      res.toOption.fold(Future.failed[ImgSrc](new NotFoundImgSrc))(Future.successful)
    }
  }

  class AttributeParseError extends Exception("Parse error from attribute")
  def getAttribution(fName: String)(implicit wiki: Wikipedia, ec: ExecutionContext): Future[ImgAttr] = {
    wiki.getImageAttribution(fName).flatMap { res =>
      val json = JsonMethods.parse(res.body)
      val JObject(xs) = json \\ "extmetadata"
      val map = xs.toMap.mapValues { x => (x \ "value").extract[String] }
      ImgAttr.fromMap(fName, map).fold(Future.failed[ImgAttr](new AttributeParseError))(Future.successful)
    }
  }
}
