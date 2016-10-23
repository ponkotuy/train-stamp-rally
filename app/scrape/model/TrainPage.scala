package scrape.model

import scala.util.Try
import scala.xml.{NodeSeq, Text}

case class TrainPage(
    name: String,
    number: String,
    code: Option[Int],
    stops: List[StopDetail],
    url: String,
    car: List[String],
    remark: List[String],
    day: String)

object TrainPage {
  import utils.ParseHelper._

  def fromXML(xml: NodeSeq, url: String): Option[TrainPage] = {
    val container = xml \\ "div" filter(_ \ "@id" contains Text("container02"))
    for {
      bigTable <- (container \ "table").lift(4)
      trs = bigTable \ "tbody" \ "tr" \ "td" \ "table" \ "tbody" \ "tr" \ "td" \ "table" \ "tbody" \ "tr"
      name <- extractValueFromTr(trs, 0)(norm)
      number <- extractValueFromTr(trs, 1)(norm)
      code = extractValueFromTr(trs, 2)(norm).flatMap { it => Try(it.toInt).toOption }
      car <- extractValueFromTr(trs, 3)(normList(','))
      remark <- extractValueFromTr(trs, 4)(normList(','))
      day <- extractValueFromTr(trs, 5)(norm)
      stops = trs.view(8, trs.length).flatMap(StopDetail.fromXML).toList
    } yield { TrainPage(name, number, code, stops, url, car, remark, day) }
  }

  private def extractValueFromTr[A](xml: NodeSeq, index: Int)(f: String => A): Option[A] = for {
    line <- xml.lift(index)
    raw <- (line \ "td").lift(1)
  } yield f((raw \ "span").text)
}
