package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Station(id: Long, name: String, rank: StationRank, geo: Option[StationGeo] = None) {
  def save()(implicit session: DBSession): Long = Station.save(this)
  def update()(implicit session: DBSession): Long = Station.update(this)
}

object Station extends SkinnyCRUDMapperWithId[Long, Station] {
  override val defaultAlias: Alias[Station] = createAlias("s")

  override def extract(rs: WrappedResultSet, n: ResultName[Station]): Station = autoConstruct(rs, n, "geo")

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  val geoRef = belongsToWithFkAndJoinCondition[StationGeo](
    right = StationGeo,
    fk = "id",
    on = sqls.eq(Station.defaultAlias.id, StationGeo.defaultAlias.stationId),
    merge = (st, geo) => st.copy(geo = geo)
  )

  def save(station: Station)(implicit session: DBSession): Long =
    createWithAttributes(params(station): _*)

  def update(station: Station)(implicit session: DBSession): Int =
    updateById(station.id).withAttributes(params(station): _*)

  def params(s: Station) = Seq('name -> s.name, 'rank -> s.rank.value)

  def findByName(name: String)(implicit session: DBSession): Option[Station] =
    findBy(sqls.eq(column.name, name))
}
