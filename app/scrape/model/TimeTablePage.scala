package scrape.model

import java.text.Normalizer

import utils.Zip

import scala.collection.GenIterable
import scala.xml._

case class TimeTablePage(title: Title, trains: GenIterable[Train])

object TimeTablePage {
  def fromXML(xml: NodeSeq) = {
    val columns = for {
      body <- xml \ "body"
      wrapper <- divIdFilter("wrapper")(body)
      container <- divIdFilter("container02")(wrapper)
      tables <- container \ "table" \ "tbody" \ "tr"
      tdw90 <- tdWidthFilter("90%")(tables)
      column <- tdw90 \ "table" \ "tbody" \ "tr" \ "td" \ "table" \ "tbody" \ "tr"
    } yield column
    require(columns.size >= 6, "Parse error")

    val title = Title.fromXML(columns(0))

    val numbers = ((columns(1) \\ "span").tail \ "span").map(_.text)
    val part1 = columns(2) \ "td"
    val types = spanClsFilter("s")(part1).map(_.text.replaceAll("\\[|\\]", ""))
    val names = spanClsFilter("m")(part1).map(_.text)
    val specials = ((columns(3) \\ "span").tail \ "span").map { it =>
      norm(it.text).trim.nonEmpty
    }
    val urls = (columns(4) \ "td" \ "span" \ "a").map { it => (it \ "@href").text }

    val part2 = columns(6) \ "td"
    val stations = (part2.head \ "span" \ "span").text.lines.map(_.trim).toList.tail.init
    val stoptypes = part2.tail.head.text.filter { !_.isWhitespace }.map { _.toString }
    val stoptimes = part2.tail.tail.map { it =>
      (it \ "span").map(_.text)
    }
    val createStops = CreateStops(stations, stoptypes, title.line)
    val stops = stoptimes.map(createStops.create)

    val trains = Zip.zip6(names, numbers, types, stops, urls, specials).map {
      case (name, num, typ, stop, url, sp) => {
        Train(name, num, typ, stop, normalizeURL(url), sp)
      }
    }
    TimeTablePage(title, trains)
  }

  private def norm(str: String) = Normalizer.normalize(str, Normalizer.Form.NFKC)
  private def filter(arg: String)(element: String)(text: String)(xs: NodeSeq) =
    xs \ arg filter (_ \ element contains Text(text))
  private def divIdFilter(id: String)(xs: NodeSeq) = filter("div")("@id")(id)(xs)
  private def tdWidthFilter(width: String)(xs: NodeSeq) =
    filter("td")("@width")(width)(xs)
  private def spanClsFilter(cls: String)(xs: NodeSeq) = filter("span")("@class")(cls)(xs)
  private def normalizeURL(url: String) = url.replace("../../", "/newdata/")
}
