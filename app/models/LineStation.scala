package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class LineStation(id: Long, lineId: Long, stationId: Long, km: Double) {
  def save()(implicit session: DBSession): Long = LineStation.save(this)
}

object LineStation extends SkinnyCRUDMapperWithId[Long, LineStation] {
  override def defaultAlias: Alias[LineStation] = createAlias("ls")

  override def extract(rs: WrappedResultSet, n: ResultName[LineStation]): LineStation = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(ls: LineStation)(implicit session: DBSession): Long =
    createWithAttributes('lineId -> ls.lineId, 'stationId -> ls.stationId, 'km -> ls.km)
}
