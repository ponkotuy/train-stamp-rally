package models

import scalikejdbc.TypeBinder

sealed abstract class TrainType(val value: Int)

object TrainType {
  case object Local extends TrainType(1)
  case object Rapid extends TrainType(2)
  case object Express extends TrainType(3)
  case object Shinkansen extends TrainType(4)

  def values = Seq(Local, Rapid, Express, Shinkansen)
  def find(v: Int): Option[TrainType] = values.find(_.value == v)

  implicit def typeBinder: TypeBinder[Int] = TypeBinder.int
  implicit val impl: TypeBinder[TrainType] = TypeBinder(_ getInt _)(_ getInt _).map(find).map(_.getOrElse(Local))

}
