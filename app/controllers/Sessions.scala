package controllers

import javax.inject.Inject

import authes.Authenticator
import com.github.tototoshi.play2.json4s.Json4s
import org.json4s.DefaultFormats
import play.api.mvc.InjectedController
import queries.LoginEmail
import scalikejdbc.AutoSession

import scala.concurrent.ExecutionContext

class Sessions @Inject() (json4s: Json4s, implicit val ec: ExecutionContext) extends InjectedController with Authenticator {
  import json4s._

  implicit val formats = DefaultFormats

  def login() = Action(json) { implicit req =>
    val result = for {
      login <- req.body.extractOpt[LoginEmail]
      account <- login.authenticate()(AutoSession)
    } yield account
    result.fold(authenticationFailed) { account => gotoLoginSucceeded(account) }
  }

  def logout() = Action { implicit req =>
    gotoLogoutSucceeded()
  }
}
