package authes

import scalikejdbc.TypeBinder

sealed abstract class Role(val value: Int) extends Authority

object Role {
  case object Disabled extends Role(-1) {
    override def auth(userAuthority: Authority): Boolean = true
  }

  case object Administrator extends Role(0) {
    override def auth(userAuthority: Authority): Boolean = userAuthority == Administrator
  }

  case object NormalUser extends Role(1) {
    override def auth(userAuthority: Authority): Boolean = userAuthority != Disabled
  }

  val values = Seq(Disabled, Administrator, NormalUser)
  def find(value: Int): Option[Role] = values.find(_.value == value)

  implicit def typeBinder: TypeBinder[Int] = TypeBinder.int
  implicit val impl: TypeBinder[Role] = TypeBinder(_ getInt _)(_ getInt _).map { i => find(i).getOrElse(NormalUser) }
}
