package scrape.model

import scala.xml.NodeSeq
import scala.collection.breakOut

case class StopDetail(
    name: String,
    url: String,
    code: Option[Int],
    no: Option[String],
    departure: TrainRun,
    arrive: TrainRun)

object StopDetail {
  import utils.ParseHelper._

  val ReCode = """\((\d+)\)""".r

  def fromXML(xml: NodeSeq): Option[StopDetail] = {
    val tds: Seq[String] = (xml \ "td").map { elem => (elem \ "span").text }(breakOut)
    for {
      url <- (xml \\ "a" \ "@href").headOption
      sta <- tds.lift(0)
      stop <- tds.lift(1)
      xs = normList('\n')(sta)
      ys = normList('\n')(stop)
      if 2 <= ys.length && 1 <= xs.length
      name = xs.head
      code = xs.lift(1).flatMap(ReCode.findFirstMatchIn(_)).map(_.group(1).toInt)
      Seq(arr, dep) = ys.map(TrainRun.fromString)
      no = tds.lift(2).map(norm).filter(_.nonEmpty)
    } yield StopDetail(name, normURL(url.text), code, no, dep, arr)
  }

  def normURL(url: String) = url.replace("../../", "/newdata/station/")
}

sealed abstract class TrainRun

object TrainRun {
  val ReTime = """(\d\d)\:(\d\d).*""".r
  case class Time(hour: Int, minutes: Int) extends TrainRun
  case object Pass extends TrainRun
  case object NotService extends TrainRun

  def fromString(str: String): TrainRun =
    str match {
      case "レ" => Pass
      case ReTime(hour, minutes) => Time(hour.toInt, minutes.toInt)
      case _ => NotService
    }
}
