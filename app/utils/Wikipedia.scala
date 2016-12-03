package utils

import net.liftweb.util.Html5
import play.api.libs.ws.WSClient

import scala.xml.Elem

class Wikipedia(ws: WSClient) {
  import Wikipedia._
  def get(params: Seq[(String, String)]) = {
    val request = ws.url(URL)
    request.withQueryString(params ++ DefaultParams:_*).get()
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
    } yield url.split('-').tail.mkString("-").replace('_', ' ')
  }
}

case class ImgAttr(fName: String, name: String, artist: Elem, licenseShortName: String, licenseUrl: String, credit: Elem) {
  def attribution = <span>By {artist} ({credit}) [<a href={licenseUrl} target="_blank">{licenseShortName}</a>], <a href={url} target="_blank">via Wikimedia Commons</a></span>
  def url = s"https://commons.wikimedia.org/wiki/File:${fName}"
}

object ImgAttr {
  def fromMap(fName: String, map: Map[String, String]): Option[ImgAttr] = {
    for {
      name <- map.get("ObjectName")
      artist <- map.get("Artist").flatMap(toXML)
      short <- map.get("LicenseShortName")
      url <- map.get("LicenseUrl")
      credit <- map.get("Credit").flatMap(toXML)
    } yield ImgAttr(fName, name, artist, short, url, credit)
  }

  def toXML(str: String): Option[Elem] = Html5.parse(str).toOption
}
