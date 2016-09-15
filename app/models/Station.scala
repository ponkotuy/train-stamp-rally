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
