package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

final case class StopStation(
    id: Long,
    diagramId: Long,
    lineStationId: Long,
    arrival: Option[Int],
    departure: Option[Int],
    diagram: Option[Diagram] = None
) {
  def save()(implicit session: DBSession): Long = StopStation.save(this)
}

object StopStation extends SkinnyCRUDMapperWithId[Long, StopStation] {
  override val defaultAlias: Alias[StopStation] = createAlias("ss")

  override def extract(rs: WrappedResultSet, n: ResultName[StopStation]): StopStation = autoConstruct(rs, n, "diagram")

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  lazy val diagramRef = belongsTo[Diagram](
    right = Diagram,
    merge = (ss, diagram) => ss.copy(diagram = diagram)
  )

  def save(ss: StopStation)(implicit session: DBSession): Long =
    createWithAttributes(
      'diagramId -> ss.diagramId,
      'lineStationId -> ss.lineStationId,
      'arrival -> ss.arrival,
      'departure -> ss.departure
    )
}
