package models

import authes.Role
import org.json4s.CustomSerializer
import org.json4s.JsonDSL._
import responses.AccountMinimal
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Account(
    id: Long,
    name: String,
    email: String,
    role: Role,
    password: String
) {
  def save()(implicit session: DBSession): Long = Account.save(this)
  def minimal = AccountMinimal(id, name)
}

object Account extends SkinnyCRUDMapperWithId[Long, Account] {
  override val defaultAlias: Alias[Account] = createAlias("a")
  override def extract(rs: WrappedResultSet, n: ResultName[Account]): Account = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(account: Account)(implicit session: DBSession): Long = {
    createWithAttributes(
      'name -> account.name,
      'email -> account.email,
      'role -> account.role.value,
      'password -> account.password
    )
  }
}

object AccountSerializer extends CustomSerializer[Account](format => (
  { PartialFunction.empty },
  {
    case x: Account =>
      ("id" -> x.id) ~ ("name" -> x.name) ~ ("role" -> x.role.value)
  }
))
