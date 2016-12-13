package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class LineStation(
    id: Long,
    lineId: Long,
    stationId: Long,
    km: Double,
    line: Option[Line] = None,
    station: Option[Station] = None
) {
  def save()(implicit session: DBSession): Long = LineStation.save(this)
}

object LineStation extends SkinnyCRUDMapperWithId[Long, LineStation] {
  override val defaultAlias: Alias[LineStation] = createAlias("ls")

  override def extract(rs: WrappedResultSet, n: ResultName[LineStation]): LineStation =
    autoConstruct(rs, n, "line", "station")

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  lazy val lineRef = belongsTo[Line](
    right = Line,
    merge = (ls, line) => ls.copy(line = line)
  )

  lazy val stationRef = belongsTo[Station](
    right = Station,
    merge = (ls, station) => ls.copy(station = station)
  )

  def save(ls: LineStation)(implicit session: DBSession): Long =
    createWithAttributes('lineId -> ls.lineId, 'stationId -> ls.stationId, 'km -> ls.km)
}
