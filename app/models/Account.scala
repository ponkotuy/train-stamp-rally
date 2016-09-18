package models

import authes.Role
import scalikejdbc.WrappedResultSet
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Account(
    id: Long,
    name: String,
    role: Role
)

object Account extends SkinnyCRUDMapperWithId[Long, Account] {
  override def defaultAlias: Alias[Account] = createAlias("a")
  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[Account]): Account = ???

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong
}
