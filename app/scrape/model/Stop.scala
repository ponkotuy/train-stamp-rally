package scrape.model

import utils.Zip

import scala.collection.GenIterable
import scala.math.Ordered

final case class Stop(name: String, line: String, typ: Stop.StopType, time: Stop.Time)
object Stop {
  sealed abstract class StopType(val str: String) extends Product with Serializable

  object StopType {
    case object Arrival extends StopType("着")
    case object Departure extends StopType("発")

    val values = Vector(Arrival, Departure)
    def fromString(str: String): Option[StopType] = values.find(_.str == str)
  }

  final case class Time(hour: Int, minutes: Int) extends Ordered[Time] {
    def minuteOfDay = hour * 60 + minutes
    def compare(that: Time) = this.minuteOfDay - that.minuteOfDay
  }

  object Time {
    def fromString(str: String): Option[Time] = str.split(':') match {
      case Array(h, m) => Some(Time(h.toInt, m.toInt))
      case _ => None
    }
  }
}

final case class CreateStops(
    stations: GenIterable[String],
    stoptypes: GenIterable[String],
    line: String
) {
  import Stop._
  type Data = (String, String, String)
  def create(times: GenIterable[String]): List[Stop] = {
    def f(xs: GenIterable[Data], before: String): List[Stop] = xs match {
      case y @ (nameRaw: String, typ: String, timeRaw: String) :: ys => {
        val name = nameRaw match {
          case "〃" => before
          case n: String => n
        }
        val result = for {
          time <- Time.fromString(timeRaw)
          stopType <- StopType.fromString(typ)
        } yield Stop(name, line, stopType, time) :: f(ys, name)
        result.getOrElse(Nil)
      }
      case _ => Nil
    }
    f(Zip.zip3(stations, stoptypes, times), "")
  }
}
