package controllers

import com.amazonaws.services.simpleemail.model.{Body, Content, Destination}
import com.github.tototoshi.play2.json4s.native.Json4s
import com.google.inject.Inject
import models.{Account, PasswordReset}
import org.json4s.{DefaultFormats, Formats}
import play.api.Configuration
import play.api.mvc.{Action, Controller, Result}
import queries.{BCryptEncoder, RequestReset, ResetPassword}
import scalikejdbc.{AutoSession, DB, sqls}
import utils.{Config, Mail, MyAmazonSES}

import scala.concurrent.duration._

class Passwords @Inject() (json4s: Json4s, _config: Configuration) extends Controller {
  import Passwords._
  import Responses._
  import json4s._

  implicit val formats: Formats = DefaultFormats
  val conf = new Config(_config)
  lazy val ses: Option[MyAmazonSES] = conf.amazon.flatMap(_.ses)

  def request() = Action(json) { implicit req =>
    import models.DefaultAliases.a
    import utils.EitherUtil.eitherToRightProjection
    val result = for {
      rr <- req.body.extractOpt[RequestReset].toRight(JSONParseError)
      account <- Account.findBy(sqls.eq(a.email, rr.email)).toRight(notFound(s"email(${rr.email})"))
    } yield {
      PasswordReset.deleteByAccountId(account.id)(AutoSession)
      val reset = PasswordReset.fromAccountId(account.id)
      reset.save()(AutoSession)
      val mes = message(reset.secret)
      val result = for {
        from <- conf.mail
        mail = Mail(new Destination().withToAddresses(account.email), Title, mes, from)
        client <- ses
      } yield client.send(mail)
      result.fold(InternalServerError("Sending mail error"))(_ => Success)
    }
    result.merge
  }

  def reset() = Action(json) { implicit request =>
    import models.DefaultAliases.pr
    import utils.EitherUtil.eitherToRightProjection
    val result: Either[Result, Result] = for {
      req <- request.body.extractOpt[ResetPassword].toRight(JSONParseError)
      db <- PasswordReset.findBy(sqls.eq(pr.secret, req.secret)).toRight(notFound(s"PasswordReset secret: ${req.secret}")).right
      _ <- Either.cond(System.currentTimeMillis() - 1.days.toMillis < db.created, Left(()), RequestTimeout("Timeout"))
      account <- Account.findById(db.accountId).toRight(notFound(s"Account: ${db.accountId}"))
    } yield {
      DB localTx { implicit session =>
        account.copy(password = BCryptEncoder(req.password)).update()
        PasswordReset.deleteByAccountId(account.id)
      }
      Success
    }
    result.merge
  }
}

object Passwords {

  val Title = content("TrainStampRallyのパスワードリセット")

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
         |https://train.ponkotuy.com/auth/password_reset.html?secret=${secret}
         |
         |有効期間は1日間のみです。それ以降は再送してください
      """.stripMargin
    )
  )

  def content(data: String) = new Content().withData(data)
}
