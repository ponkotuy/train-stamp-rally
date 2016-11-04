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

  def fromXML(xml: NodeSeq, url: String): Either[String, TrainPage] = {
    import scala.language.implicitConversions
    implicit def rightBias[A, B](e: Either[A, B]) = e.right
    for {
      container <- (xml \\ "div" find(_ \ "@id" contains Text("container02"))).toRight("container")
      table <- (container \\ "table" find(_ \ "@cellpadding" contains Text("5"))).toRight("table")
      trs = table \ "tbody" \ "tr"
      name <- extractValueFromTr(trs, 0)(norm).toRight("name")
      number <- extractValueFromTr(trs, 1)(norm).toRight("number")
      code = extractValueFromTr(trs, 2)(norm).flatMap { it => Try(it.toInt).toOption }
      car <- extractValueFromTr(trs, 3)(normList(',')).toRight("car")
      remark <- extractValueFromTr(trs, 4)(normList(',')).toRight("remark")
      day <- extractValueFromTr(trs, 5)(norm).toRight("day")
      stops = trs.view(8, trs.length).flatMap(StopDetail.fromXML).toList
    } yield { TrainPage(name, number, code, stops, url, car, remark, day) }
  }

  private def extractValueFromTr[A](xml: NodeSeq, index: Int)(f: String => A): Option[A] = for {
    line <- xml.lift(index)
    raw <- (line \ "td").lift(1)
  } yield f((raw \ "span").text)
}
