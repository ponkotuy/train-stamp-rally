package utils

import scalikejdbc.TypeBinder

import scala.util.Try

case class TrainTime(hour: Int, minutes: Int) extends Ordered[TrainTime] {
  assert(0 <= hour && hour < 24)
  assert(0 <= minutes && minutes < 60)

  override def toString: String = f"${hour}%02d${minutes}%02d"

  override def compare(that: TrainTime): Int = TrainTime.ordering.compare(this, that)

  def addMinutes(add: Int): TrainTime = {
    val min = mod(hour * 60 + minutes + add, 24 * 60)
    TrainTime(min / 60, min % 60)
  }

  def mod(x: Int, y: Int): Int = {
    val z = x % y
    if(z < 0) z + y else z
  }
}

object TrainTime {
  val ordering = Ordering.by(unapply)
  val Default = TrainTime(6, 0)

  def fromString(str: String): Option[TrainTime] =
    Try {
      val raw = str.toInt
      TrainTime(raw / 100, raw % 100)
    }.toOption


  implicit def typeBinder: TypeBinder[String] = TypeBinder.string
  implicit val impl: TypeBinder[TrainTime] =
    TypeBinder(_ getString _)(_ getString _).map(fromString).map(_.getOrElse(Default))
}
