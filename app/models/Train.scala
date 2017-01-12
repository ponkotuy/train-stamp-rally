package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.TrainTime

final case class Train(
    id: Long,
    diagramId: Long,
    start: TrainTime,
    diagram: Option[Diagram] = None,
    stops: Seq[StopStation] = Nil
) {
  def save()(implicit session: DBSession): Long = Train.save(this)
}

object Train extends SkinnyCRUDMapperWithId[Long, Train] {
  override def defaultAlias: Alias[Train] = createAlias("t")
  lazy val t = defaultAlias

  override def extract(rs: WrappedResultSet, n: ResultName[Train]): Train = autoConstruct(rs, n, "diagram", "stops")

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  lazy val diagramRef = belongsTo[Diagram](
    right = Diagram,
    merge = (t, d) => t.copy(diagram = d)
  )

  lazy val stopStationRef = hasMany[StopStation](
    many = StopStation -> StopStation.defaultAlias,
    on = (t, ss) => sqls.eq(t.diagramId, ss.diagramId),
    merge = (t, sss) => t.copy(stops = sss)
  )

  def allDiagramIds()(implicit session: DBSession) = withSQL {
    select(t.diagramId).from(Train as t)
  }.map(_.long(1)).list().apply()

  def save(t: Train)(implicit session: DBSession): Long =
    createWithAttributes('diagramId -> t.diagramId, 'start -> t.start.toString)
}
