package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Diagram(
    id: Long,
    name: String,
    trainType: TrainType,
    subType: String
) {
  def save()(implicit session: DBSession): Long = Diagram.save(this)
}

object Diagram extends SkinnyCRUDMapperWithId[Long, Diagram] {
  override def defaultAlias: Alias[Diagram] = createAlias("d")

  override def extract(rs: WrappedResultSet, n: ResultName[Diagram]): Diagram = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(d: Diagram)(implicit session: DBSession): Long =
    createWithAttributes('name -> d.name, 'trainType -> d.trainType.value, 'subType -> d.subType)
}
