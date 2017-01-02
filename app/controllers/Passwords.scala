package controllers

import com.google.inject.Inject
import models.Account
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import scalikejdbc.sqls
import utils.Mail

class Passwords @Inject()() extends Controller {
  import Responses._
  import Passwords._

  def request() = Action { implicit req =>
    import models.DefaultAliases.a
    EmailForm.bindFromRequest().fold(badRequest(_), { email =>
      Account.findBy(sqls.eq(a.email, email)).fold(notFound(s"email(${email})")) { account =>

        new Mail(account.email, )
      }
    })
  }
  def reset(secret: String) = {

  }
}

object Passwords {
  import play.api.data.Forms.text

  val EmailForm = Form(text)
  val Title = "TrainStampRallyのパスワードリセット"
  def message(secret: String) =
    s"""
      |※このメールはAmazonSESによって送信されています
      |
      |TrainStampRallyのパスワードリセットメールです。
      |心当たりのない方は無視するか返信で教えていただけると幸いです。
      |
      |以下のURLにてパスワードを再設定することができます。
      |
      |${routes.Passwords.reset(secret)}
    """.stripMargin
}
