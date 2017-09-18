package utils

import java.net.URLDecoder

import models.{Attr, ImageAttribute}
import net.liftweb.util.Html5
import play.api.libs.ws.WSClient

import scala.xml.transform.RewriteRule
import scala.xml.{Elem, Node}

class Wikipedia(val ws: WSClient) {
  import Wikipedia._
  def get(params: Seq[(String, String)]) = {
    val request = ws.url(URL)
    request.addQueryStringParameters(params ++ DefaultParams: _*).get()
  }

  def getInfo(title: String) = get(Seq("prop" -> "info", "titles" -> title))

  def getContent(title: String) =
    get(Seq("prop" -> "revisions", "rvprop" -> "content", "titles" -> title))

  def getXML(title: String) =
    get(Seq("prop" -> "revisions", "rvprop" -> "content", "titles" -> title, "rvparse" -> ""))

  def getHTML(title: String) = {
    val url = s"https://ja.wikipedia.org/wiki/${title}"
    ws.url(url).get()
  }

  def getImageAttribution(name: String) =
    get(Seq("prop" -> "imageinfo", "iiprop" -> "extmetadata", "titles" -> s"File:${name}"))
}

object Wikipedia {
  val DefaultParams = Map("action" -> "query", "format" -> "json")
  val URL = "https://ja.wikipedia.org/w/api.php"

  def parseSrcSet(set: String): Seq[ImgSrc] = {
    set.split(',').map { one =>
      val Array(url, scale) = one.trim.split(' ')
      ImgSrc(url, scale.init.toDouble)
    }
  }
}

case class ImgSrc(url: String, scale: Double) {
  def withScheme = "https:" + url

  lazy val origin: Option[String] = {
    for {
      fName <- url.split('/').lastOption
    } yield URLDecoder.decode(url.split('-').tail.mkString("-").replace('_', ' '), "UTF-8")
  }
}

case class ImgAttr(
    fileName: String,
    name: String,
    artist: Node,
    licenseShortName: String,
    licenseUrl: String,
    credit: Node
) extends Attr {
  def imageAttribute(stationId: Long, now: Long): ImageAttribute =
    new ImageAttribute(stationId, fileName, name, artist, licenseShortName, licenseShortName, credit, now)
}

object ImgAttr {
  def fromMap(fName: String, map: Map[String, String]): Option[ImgAttr] = {
    for {
      name <- map.get("ObjectName")
      artist <- map.get("Artist").flatMap(toXML).map(pToSpan(_))
      short <- map.get("LicenseShortName")
      url <- map.get("LicenseUrl")
      credit <- map.get("Credit").flatMap(toXML).map(pToSpan(_))
    } yield ImgAttr(fName, name, artist, short, url, credit)
  }

  def toXML(str: String): Option[Elem] = Html5.parse(str).toOption

  object pToSpan extends RewriteRule {
    override def transform(n: Node): Seq[Node] = n match {
      case Elem(pre, "p", attr, scope, child @ _*) => Elem(pre, "span", attr, scope, false, child: _*)
      case other => other
    }
  }
}
