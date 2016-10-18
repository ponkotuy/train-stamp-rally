package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.AccountSerializer
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.{CreateAccount, LoginEmail}
import scalikejdbc.AutoSession

import scala.concurrent.ExecutionContext

class Sessions @Inject()(json4s: Json4s, implicit val ec: ExecutionContext)
    extends Controller
    with AuthConfigImpl
    with AuthElement
    with LoginLogout {
  import json4s._
  import Responses._

  implicit val formats = DefaultFormats + AccountSerializer

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

  def show() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(loggedIn))
  }

  def createAccount() = Action(json) { req =>
    req.body.extractOpt[CreateAccount].fold(JSONParseError) { account =>
      account.account.save()(AutoSession)
      Success
    }
  }
}
