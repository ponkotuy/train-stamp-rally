package queries

import authes.Role
import models.Account
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

case class CreateAccount(name: String, email: String, password: String) {
  def account: Account = {
    val encoded = BCryptEncoder(password)
    Account(0L, name, email, Role.NormalUser, encoded)
  }
}

object BCryptEncoder {
  val bcrypt = new BCryptPasswordEncoder()
  def apply(pass: String) = bcrypt.encode(pass)
}
