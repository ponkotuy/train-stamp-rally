package models

import org.json4s.CustomSerializer
import org.json4s.JsonDSL._
import scalikejdbc.TypeBinder

sealed abstract class TrainType(val value: Int, val name: String) {
  def toJson = ("value" -> value) ~ ("name" -> name)
}

object TrainType {
  case object Local extends TrainType(1, "普通")
  case object Rapid extends TrainType(2, "快速")
  case object Express extends TrainType(3, "特急")
  case object Shinkansen extends TrainType(4, "新幹線")

  def values = Seq(Local, Rapid, Express, Shinkansen)
  def find(v: Int): Option[TrainType] = values.find(_.value == v)

  implicit def typeBinder: TypeBinder[Int] = TypeBinder.int
  implicit val impl: TypeBinder[TrainType] = TypeBinder(_ getInt _)(_ getInt _).map(find).map(_.getOrElse(Local))
}

class TrainTypeSerializer extends CustomSerializer[TrainType](format => (
    {PartialFunction.empty},
    {
      case x: TrainType =>
        ("value" -> x.value) ~ ("name" -> x.name)
    }
    )
)
