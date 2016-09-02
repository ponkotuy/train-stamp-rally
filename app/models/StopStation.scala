package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class StopStation(
    id: Long,
    diagramId: Long,
    lineStationId: Long,
    minutes: Int
) {
  def save()(implicit session: DBSession): Long = StopStation.save(this)
}

object StopStation extends SkinnyCRUDMapperWithId[Long, StopStation] {
  override def defaultAlias: Alias[StopStation] = createAlias("ss")

  override def extract(rs: WrappedResultSet, n: ResultName[StopStation]): StopStation = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(ss: StopStation)(implicit session: DBSession): Long =
    createWithAttributes('diagramId -> ss.diagramId, 'lineStationId -> ss.lineStationId, 'minutes -> ss.minutes)
}
