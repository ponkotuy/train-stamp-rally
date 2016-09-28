package utils

import scalikejdbc.TypeBinder

// day: 1-indexed
case class MissionTime(day: Int, hour: Int, minutes: Int) extends Ordered[MissionTime] {
  assert(1 <= day)
  assert(0 <= hour && hour < 24)
  assert(0 <= minutes && minutes < 60)

  def setTime(time: TrainTime): MissionTime = {
    if(trainTime < time) copy(hour = time.hour, minutes = time.minutes)
    else copy(day = day + 1, hour = time.hour, minutes = time.minutes)
  }

  override def toString: String = f"${day}-${hour}%02d:${minutes}%02d"

  lazy val trainTime = TrainTime(hour, minutes)

  override def compare(that: MissionTime): Int = MissionTime.ordering.compare(this, that)
}

object MissionTime {
  val Regex = """\d+\-\d+\:\d+""".r
  val ordering = Ordering.by(unapply)
  val Default = MissionTime(1, 6, 0)

  def fromString(str: String): Option[MissionTime] = str match {
    case Regex(day, hour, minutes) => Some(MissionTime(day.toInt, hour.toInt, minutes.toInt))
    case _ => None
  }

  implicit def typeBinder: TypeBinder[String] = TypeBinder.string
  implicit val impl: TypeBinder[MissionTime] =
    TypeBinder(_ getString _)(_ getString _).map(fromString).map(_.getOrElse(Default))
}
