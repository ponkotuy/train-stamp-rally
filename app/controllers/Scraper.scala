package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import net.liftweb.util.Html5
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import scrape.model.{TimeTablePage, TrainPage}

import scala.io.Codec
import scala.xml._

class Scraper @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._

  implicit val formats = DefaultFormats

  def timeTable(url: String) = StackAction(AuthorityKey -> Administrator) { implicit req =>
    val xml = loadXML(url)
    TimeTablePage.fromXML(xml)
    ???
  }

  def train(lineId: Long, trainId: Long) = StackAction(AuthorityKey -> Administrator) { implicit req =>
    val host = "http://www.ekikara.jp"
    val address = s"/newdata/detail/${lineId}/${trainId}.htm"
    val train = TrainPage.fromXML(loadXML(s"${host}${address}"), address)
    Ok(Extraction.decompose(train.get))
  }

  private[this] def loadXML(url: String): NodeSeq = {
    val html = scala.io.Source.fromURL(url)(Codec("Shift_JIS")).mkString
    Html5.parse(html) openOr NodeSeq.Empty
  }
}
