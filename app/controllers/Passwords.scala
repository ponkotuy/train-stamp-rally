package controllers

import com.amazonaws.services.simpleemail.model.{Body, Content}
import com.google.inject.Inject
import models.{Account, PasswordReset}
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import scalikejdbc.{AutoSession, sqls}
import utils.Mail

class Passwords @Inject()() extends Controller {
  import Responses._
  import Passwords._

  def request() = Action { implicit req =>
    import models.DefaultAliases.a
    EmailForm.bindFromRequest().fold(badRequest(_), { email =>
      Account.findBy(sqls.eq(a.email, email)).fold(notFound(s"email(${email})")) { account =>
        PasswordReset.deleteBy(sqls.eq(PasswordReset.column.accountId, account.id))
        val reset = PasswordReset.fromAccountId(account.id)
        reset.save()(AutoSession)
        val mes = message(reset.secret)
        new Mail(account.email, Title, mes, )
      }
    })
  }
  def reset(secret: String) = {

  }
}

object Passwords {
  import play.api.data.Forms.text

  val EmailForm = Form(text)
  val Title = content("TrainStampRallyのパスワードリセット")
  val From = ""

  def message(secret: String) = new Body().withText(
    content(
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
    )
  )

  def content(data: String) = new Content().withData(data)
}
