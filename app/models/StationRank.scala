package models

import org.json4s.CustomSerializer
import org.json4s.JsonDSL._
import scalikejdbc.TypeBinder

sealed abstract class StationRank(val value: Int, val name: String)

object StationRank {
  case object Top extends StationRank(1, "中心駅") // Tokyo, Nagoya, Osaka
  case object Major extends StationRank(2, "主要駅") // Shinjuku, Nagano, Sapporo etc...
  case object Large extends StationRank(3, "特急駅") // Limited Express stops
  case object Middle extends StationRank(4, "快速駅") // Rapid stops
  case object Local extends StationRank(5, "末端駅") // others

  def values = Seq(Top, Major, Large, Middle, Local)
  def find(v: Int): Option[StationRank] = values.find(_.value == v)

  implicit def typeBinder: TypeBinder[Int] = TypeBinder.int
  implicit val impl: TypeBinder[StationRank] = TypeBinder(_ getInt _)(_ getInt _).map { i => find(i).getOrElse(Local) }
}

object StationRankSerializer extends CustomSerializer[StationRank](format => (
    {PartialFunction.empty},
    {
      case x: StationRank =>
        ("value" -> x.value) ~ ("name" -> x.name)
    }
    )
)
