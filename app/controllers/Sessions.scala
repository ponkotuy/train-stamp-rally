package controllers

import authes.AuthConfigImpl
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import org.json4s.DefaultFormats
import play.api.mvc.{Action, Controller}
import queries.{CreateAccount, LoginEmail}
import scalikejdbc.AutoSession

import scala.concurrent.ExecutionContext

class Sessions @Inject()(json4s: Json4s, implicit val ec: ExecutionContext) extends Controller with AuthConfigImpl {
  import json4s._
  import Responses._

  implicit val formats = DefaultFormats

  def login() = Action.async(json) { req =>
    val result = for {
      login <- req.body.extractOpt[LoginEmail]
      account <- login.authenticate()(AutoSession)
    } yield Unit
    result.fold(authenticationFailed(req)) { _ => loginSucceeded(req) }
  }

  def account() = Action(json) { req =>
    req.body.extractOpt[CreateAccount].fold(JSONParseError) { account =>
      account.account.save()(AutoSession)
      Success
    }
  }
}
