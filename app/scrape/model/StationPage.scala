package scrape.model

import scala.collection.breakOut
import scala.xml.{Node, NodeSeq, Text}

case class StationPage(trains: Seq[StationTrain], abbr: Abbr)

object StationPage {
  import utils.ParseHelper._

  def fromXml(xml: NodeSeq): Either[String, StationPage] = {
    import scala.language.implicitConversions
    implicit def rightBias[A, B](e: Either[A, B]) = e.right
    for {
      table <- (xml \\ "table" find (_ \ "@cellpadding" contains Text("8"))).toRight("table")
      abbrTable <- (xml \\ "table" find (_ \ "@cellpadding" contains Text("1"))).toRight("abbrTable")
      abbr <- parseAbbr(abbrTable).toRight("abbr")
    } yield {
      val trs = (table \ "tbody" \ "tr").tail
      val trains = trs.flatMap(parseHour)
      StationPage(trains, abbr)
    }
  }

  private def parseHour(tr: Node): Seq[StationTrain] = {
    val result = for {
      hourTd <- tr \ "td" find (_ \ "@class" contains Text("lowBg06"))
      trainsTd <- tr \ "td" find { elem => !(elem \ "@class" contains Text("lowBg06")) }
    } yield {
      val hour = (hourTd \ "span" \ "span").text.toInt
      val minutesTrains = (trainsTd \\ "td" filter { it => (it \ "@id").nonEmpty }).flatMap(parseTrain)
      minutesTrains.map(_.withHour(hour))
    }
    result.getOrElse(Nil)
  }

  val TrainRegex = """\[(.+)\](.+)""".r
  private def parseTrain(td: Node): Seq[MinutesTrain] = {
    val result = for {
      tStr <- (td \ "span" \ "span").headOption
      minutes <- (td \\ "a").headOption
    } yield {
      val add = (td \\ "span" find (_ \ "@class" contains Text("textRed"))).map(_.text)
      tStr.text match {
        case TrainRegex(typ, dest) => MinutesTrain(dest, Some(typ), minutes.text.toInt, add)
        case _ => MinutesTrain(tStr.text, None, minutes.text.toInt, add)
      }
    }
    result.toSeq
  }

  val AbbrRegex = """(.+)\.\.\.(.+)""".r
  private def parseAbbr(table: Node): Option[Abbr] = {
    for {
      typTr <- (table \\ "tr").headOption
      typTd <- (typTr \\ "td").lift(2)
      destTr <- (table \\ "tr").lift(1)
      destTd <- (destTr \\ "td").lift(2)
    } yield Abbr(parseAbbrElem(typTd), parseAbbrElem(destTd))
  }

  private def parseAbbrElem(td: Node): Map[String, String] = {
    val text = norm(td.text).replace("[", "").replace("]", "")
    text.split(',').map(_.trim).flatMap {
      case AbbrRegex(k, v) => Some(k -> v)
      case _ => None
    }(breakOut)
  }

  private case class MinutesTrain(dest: String, typ: Option[String], minutes: Int, add: Option[String]) {
    def withHour(hour: Int): StationTrain =
      StationTrain(dest, typ, Stop.Time(hour % 24, minutes), add)
  }
}

case class StationTrain(dest: String, typ: Option[String], time: Stop.Time, add: Option[String]) {
  def replaceAbbr(abbr: Abbr) = copy(
    dest = abbr.dest.getOrElse(dest, dest),
    typ = typ.map { t => abbr.typ.getOrElse(t, t) }
  )
}

case class Abbr(typ: Map[String, String], dest: Map[String, String])
