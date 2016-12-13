package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.AccountSerializer
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.CreateAccount
import scalikejdbc.AutoSession

class Accounts @Inject() (json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._
  import Responses._

  implicit val formats = DefaultFormats + AccountSerializer

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
