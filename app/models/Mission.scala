package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Mission(id: Long, name: String, created: Long) {
  def save()(implicit session: DBSession): Long = Mission.save(this)
}

object Mission extends SkinnyCRUDMapperWithId[Long, Mission] {
  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  override def defaultAlias: Alias[Mission] = createAlias("m")

  override def extract(rs: WrappedResultSet, n: ResultName[Mission]): Mission = autoConstruct(rs, n)

  def save(mission: Mission)(implicit session: DBSession): Long =
    createWithAttributes('name -> mission.name, 'created -> mission.created)
}
