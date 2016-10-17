package scrape.model

import utils.Zip

import scala.collection.GenIterable
import scala.math.Ordered

case class Stop(name: String, line: String, typ: Stop.StopType, time: Stop.Time)
object Stop {
  type StopType = StopType.Value
  object StopType extends Enumeration {
    val Arrival, Departure = Value

    def fromString(str: String) = str match {
      case "着" => Arrival
      case "発" => Departure
    }
  }

  case class Time(hour: Int, minutes: Int) extends Ordered[Time] {
    def minuteOfDay = hour*60 + minutes
    def compare(that: Time) = this.minuteOfDay - that.minuteOfDay
  }

  object Time {
    def fromString(str: String): Option[Time] = str.split(':') match {
      case Array(h, m) => Some( Time(h.toInt, m.toInt) )
      case _ => None
    }
  }
}

case class CreateStops(
  stations: GenIterable[String],
  stoptypes: GenIterable[String],
  line: String)
{
  import Stop._
  type Data = (String, String, String)
  def create(times: GenIterable[String]): List[Stop] = {
    def f(xs: GenIterable[Data], before: String): List[Stop] = xs match {
      case y@(nameRaw: String, typ: String, timeRaw: String) :: ys => {
        val name = nameRaw match {
          case "〃" => before
          case n: String => n
        }
        val time = Time.fromString(timeRaw)
        if(time.isEmpty) return f(ys, name)
        Stop(name, line, StopType.fromString(typ), time.get) :: f(ys, name)
      }
      case _ => Nil
    }
    f(Zip.zip3(stations, stoptypes, times), "")
  }
}
