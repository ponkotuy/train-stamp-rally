package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

final case class Image(id: Long, bytes: Array[Byte], created: Long) {
  def save()(implicit session: DBSession) = Image.save(this)
}

object Image extends SkinnyCRUDMapperWithId[Long, Image] {
  override def defaultAlias: Alias[Image] = createAlias("i")

  override def extract(rs: WrappedResultSet, n: ResultName[Image]): Image = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(img: Image)(implicit session: DBSession): Long =
    createWithAttributes('bytes -> img.bytes, 'created -> img.created)
}
