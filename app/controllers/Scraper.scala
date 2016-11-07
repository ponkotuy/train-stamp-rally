package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import net.liftweb.util.Html5
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import scrape.model.{StationPage, TrainPage}

import scala.io.Codec
import scala.xml._

class Scraper @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import Scraper._
  import json4s._

  implicit val formats = DefaultFormats

  def station(lineId: String, pageName: String) = Action { implicit req =>
    val url = s"${Host}/newdata/ekijikoku/${lineId}/${pageName}.htm"
    val xml = loadXML(url)
    StationPage.fromXml(xml) match {
      case Left(str) => notFound(str)
      case Right(station) =>
        Ok(Extraction.decompose(station.trains.map(_.replaceAbbr(station.abbr))))
    }
  }

  def timeTable(lineId: String, pageName: String) = StackAction(AuthorityKey -> Administrator) { implicit req =>
    ???
  }

  def train(lineId: String, trainId: String) = StackAction(AuthorityKey -> Administrator) { implicit req =>
    val address = s"/newdata/detail/${lineId}/${trainId}.htm"
    TrainPage.fromXML(loadXML(s"${Host}${address}"), address) match {
      case Left(str) => notFound(str)
      case Right(train) => Ok(Extraction.decompose(train))
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
