package scrape.model

import java.text.Normalizer

import scala.xml._

case class Title(
  company: String,
  line: String,
  dest: String,
  direct: Title.Direction,
  week: Title.Week,
  page: Title.Page
)

object Title {
  private def norm(str: String) = Normalizer.normalize(str, Normalizer.Form.NFKC)

  def fromXML(xml: NodeSeq) = {
    val title = norm((xml \\ "span" \ "span").text)
    val splitted = title.split(' ')
    val Array(_, company, line) = splitted(0).split("\\[|\\]")
    val Array(rawDest, rawDirect) = splitted(1).split("\\(|\\)")
    val dest = rawDest.stripSuffix("方面")
    val direct = Direction.fromString(rawDirect)
    val week = Week.fromString(splitted(2))
    val page = Page.fromString(splitted(3))
    Title(company, line, dest, direct, week, page)
  }

  type Direction = Direction.Value
  object Direction extends Enumeration {
    val UP, DOWN = Value

    def fromString(str: String) = str match {
      case "上り" => UP
      case "下り" => DOWN
    }
  }

  type Week = Week.Value
  object Week extends Enumeration {
    val Weekday, Saturday, Sunday = Value

    def fromString(str: String) = str match {
      case "平日" => Weekday
      case "土曜日" => Saturday
      case "日曜日" => Sunday
    }
  }

  case class Page(now: Int, max: Int)
  object Page {
    // now/max形式
    def fromString(str: String) = {
      val raws = str.split('/')
      val now = raws(0).toInt
      val max = raws(1).takeWhile(_.isDigit).toInt
      Page(now, max)
    }
  }
}
