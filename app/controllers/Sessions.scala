package controllers

import authes.AuthConfigImpl
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.LoginLogout
import org.json4s.DefaultFormats
import play.api.mvc.{Action, Controller}
import queries.LoginEmail
import scalikejdbc.AutoSession

import scala.concurrent.ExecutionContext

class Sessions @Inject()(json4s: Json4s, implicit val ec: ExecutionContext)
    extends Controller
    with AuthConfigImpl
    with LoginLogout {
  import json4s._

  implicit val formats = DefaultFormats

  def login() = Action.async(json) { implicit req =>
    val result = for {
      login <- req.body.extractOpt[LoginEmail]
      account <- login.authenticate()(AutoSession)
    } yield account
    result.fold(authenticationFailed(req)) { account => gotoLoginSucceeded(account.id) }
  }

  def logout() = Action.async { implicit req =>
    gotoLogoutSucceeded
  }
}
