package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Station(id: Long, name: String, rank: StationRank) {
  def save()(implicit session: DBSession): Long = Station.save(this)
}

object Station extends SkinnyCRUDMapperWithId[Long, Station] {
  override def defaultAlias: Alias[Station] = createAlias("s")

  override def extract(rs: WrappedResultSet, n: ResultName[Station]): Station = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(station: Station)(implicit session: DBSession): Long =
    createWithAttributes('name -> station.name, 'rank -> station.rank.value)

  def findByName(name: String)(implicit session: DBSession): Option[Station] =
    findBy(sqls.eq(column.name, name))
}

sealed abstract class StationRank(val value: Int)

object StationRank {
  case object Top extends StationRank(1) // Tokyo, Nagoya, Osaka
  case object Major extends StationRank(2) // Shinjuku, Nagano, Sapporo etc...
  case object Large extends StationRank(3) // Limited Express stops
  case object Middle extends StationRank(4) // Rapid stops
  case object Local extends StationRank(5) // others

  def values = Seq(Top, Major, Large, Middle, Local)
  def find(v: Int): Option[StationRank] = values.find(_.value == v)

  implicit def typeBinder: TypeBinder[Int] = TypeBinder.int
  implicit val impl: TypeBinder[StationRank] = TypeBinder(_ getInt _)(_ getInt _).map { i => find(i).getOrElse(Local) }
}
