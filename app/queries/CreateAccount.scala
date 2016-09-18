package queries

import authes.Role
import models.Account
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

case class CreateAccount(name: String, email: String, password: String) {
  import CreateAccount._
  def account: Account = {
    val encoded = bcrypt.encode(password)
    Account(0L, name, email, Role.NormalUser, encoded)
  }
}

object CreateAccount {
  val bcrypt = new BCryptPasswordEncoder()
}
