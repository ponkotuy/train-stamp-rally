package models

import scalikejdbc.{DBSession, WrappedResultSet, autoConstruct}
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

final case class History(id: Long, message: String, created: Long) {
  def save()(implicit session: DBSession): Long = History.save(this)
}

object History extends SkinnyCRUDMapperWithId[Long, History] {
  override val defaultAlias: Alias[History] = createAlias("h")

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[History]): History = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id

  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(h: History)(implicit session: DBSession): Long =
    createWithAttributes('message -> h.message, 'created -> h.created)
}
