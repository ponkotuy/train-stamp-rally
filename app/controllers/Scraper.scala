package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import net.liftweb.util.Html5
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController
import scrape.model.{StationPage, TrainPage}

import scala.io.Codec
import scala.xml._

class Scraper @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import Responses._
  import Scraper._
  import json4s.implicits._

  implicit val formats = DefaultFormats

  def station(lineId: String, pageName: String) = Action { implicit req =>
    val url = s"${Host}/newdata/ekijikoku/${lineId}/${pageName}.htm"
    val xml = loadXML(url)
    StationPage.fromXml(xml) match {
      case Left(str) => notFound(str)
      case Right(station) =>
        val trains = station.trains.filterNot(_.add.contains("◆"))
        Ok(Extraction.decompose(trains.map(_.replaceAbbr(station.abbr))))
    }
  }

  def timeTable(lineId: String, pageName: String) = Action { implicit req =>
    withAuth(Administrator) { _ => ??? }
  }

  def train(lineId: String, trainId: String) = Action { implicit req =>
    withAuth(Administrator) { _ =>
      val address = s"/newdata/detail/${lineId}/${trainId}.htm"
      TrainPage.fromXML(loadXML(s"${Host}${address}"), address) match {
        case Left(str) => notFound(str)
        case Right(train) => Ok(Extraction.decompose(train))
      }
    }
  }

  private[this] def loadXML(url: String): NodeSeq = {
    val html = scala.io.Source.fromURL(url)(Codec("Shift_JIS")).mkString
    Html5.parse(html) openOr NodeSeq.Empty
  }
}

object Scraper {
  val Host = "http://www.ekikara.jp"
}
