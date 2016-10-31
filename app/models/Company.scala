package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Company(id: Long, name: String)

object Company extends SkinnyCRUDMapperWithId[Long, Company] {
  override def defaultAlias: Alias[Company] = createAlias("c")
  override def extract(rs: WrappedResultSet, n: ResultName[Company]): Company = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong
}
