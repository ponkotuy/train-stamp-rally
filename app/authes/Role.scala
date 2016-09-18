package authes

sealed abstract class Role(val value: Int)

object Role {
  case object Administrator extends Role(0)
  case object NormalUser extends Role(1)
}
