package queries

import models.Account
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import scalikejdbc._

case class LoginEmail(email: String, password: String) {
  import LoginEmail._

  def authenticate()(implicit session: DBSession): Option[Account] = {
    Account.findBy(sqls.eq(Account.column.email, email)).filter { account =>
      bcrypt.matches(password, account.password)
    }
  }
}

object LoginEmail {
  val bcrypt = new BCryptPasswordEncoder()
}
