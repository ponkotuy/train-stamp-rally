package scrape.model

import java.text.Normalizer

import scala.xml._

final case class Title(
  company: String,
  line: String,
  dest: String,
  direct: Title.Direction,
  week: Title.Week,
  page: Title.Page
)

object Title {
  private def norm(str: String) = Normalizer.normalize(str, Normalizer.Form.NFKC)

  def fromXML(xml: NodeSeq): Option[Title] = {
    val title = norm((xml \\ "span" \ "span").text)
    val splitted = title.split(' ')
    val Array(_, company, line) = splitted(0).split("\\[|\\]")
    val Array(rawDest, rawDirect) = splitted(1).split("\\(|\\)")
    val dest = rawDest.stripSuffix("方面")
    for {
      direct <- Direction.fromString(rawDirect)
      week <- Week.fromString(splitted(2))
      page = Page.fromString(splitted(3))
    } yield Title(company, line, dest, direct, week, page)
  }

  sealed abstract class Direction(val name: String) extends Product with Serializable
  object Direction {
    case object Up extends Direction("上り")
    case object Down extends Direction("下り")

    val values = Vector(Up, Down)
    def fromString(str: String): Option[Direction] = values.find(_.name == str)
  }

  sealed abstract class Week(val name: String) extends Product with Serializable
  object Week {
    case object Weekday extends Week("平日")
    case object Saturday extends Week("土曜日")
    case object Sunday extends Week("日曜日")

    val values = Vector(Weekday, Saturday, Sunday)
    def fromString(str: String): Option[Week] = values.find(_.name == str)
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
