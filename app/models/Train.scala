package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.TrainTime

case class Train(
    id: Long,
    diagramId: Long,
    start: TrainTime
)

object Train extends SkinnyCRUDMapperWithId[Long, Train] {
  override def defaultAlias: Alias[Train] = createAlias("t")

  override def extract(rs: WrappedResultSet, n: ResultName[Train]): Train = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(t: Train)(implicit session: DBSession): Long =
    createWithAttributes('diagramId -> t.diagramId, 'start -> t.start.toString)
}
