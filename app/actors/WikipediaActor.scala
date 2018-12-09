package actors

import akka.actor.Actor
import models._
import net.liftweb.util.Html5
import org.json4s.native.JsonMethods
import org.json4s.{DefaultFormats, Formats, _}
import scalikejdbc._
import utils.{ImgAttr, ImgSrc, Wikipedia}

import scala.concurrent.{ExecutionContext, Future}

class WikipediaActor(wiki: Wikipedia)(implicit ec: ExecutionContext) extends Actor {
  import WikipediaActor._

  implicit def _wiki: Wikipedia = wiki

  type ResponseSaveImage = Future[(StationImage, Attr)]

  override def receive = {
    case ImageRequest(id) =>
      val result = StationImage.findById(id).fold[Future[StationImage]](saveImage(id).map(_._1))(Future.successful)
      sender ! result
    case AttrRequest(id) =>
      if (StationImage.findById(id).map(_.imageId).contains(None)) sender ! None
      else {
        val result = ImageAttribute.findById(id).fold[Future[Attr]](saveImage(id).map(_._2))(Future.successful)
        sender ! result
      }
  }

  def saveImage(stationId: Long): ResponseSaveImage = {
    Station.findById(stationId).fold[ResponseSaveImage](Future.failed(StationNotFound(stationId))) { st =>
      val future = for {
        imgSrc <- getImgSrc(st.name)
        fName <- Future { imgSrc.origin.get }
        attr <- getAttribution(fName)
        res <- wiki.ws.url(imgSrc.withScheme).get()
      } yield {
        val now = System.currentTimeMillis()
        DB localTx { implicit session =>
          val image = Image(0L, res.bodyAsBytes.toArray, now)
          val id = image.save()(session)
          val stImage = StationImage(stationId, Some(id))
          stImage.save()(session)
          attr.imageAttribute(stationId, now).save()(session)
          (stImage.copy(image = Some(image)), attr)
        }
      }
      future.failed.foreach[Unit] { _ =>
        StationImage(stationId, None).save()(AutoSession)
      }
      future
    }
  }
}

case class ImageRequest(stationId: Long)

case class AttrRequest(stationId: Long)

case class StationNotFound(stationId: Long) extends RuntimeException(s"Station nof found. id = ${stationId}")

object WikipediaActor {
  implicit val formats: Formats = DefaultFormats

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

  class AttributeParseError(map: Map[String, String]) extends Exception(s"Parse error from attribute: ${map}")
  def getAttribution(fName: String)(implicit wiki: Wikipedia, ec: ExecutionContext): Future[ImgAttr] = {
    wiki.getImageAttribution(fName).flatMap { res =>
      val json = JsonMethods.parse(res.body)
      val JObject(xs) = json \\ "extmetadata"
      val map = xs.toMap.mapValues { x => (x \ "value").extract[String] }
      ImgAttr.fromMap(fName, map).fold(Future.failed[ImgAttr](new AttributeParseError(map)))(Future.successful)
    }
  }
}
