package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import models.{Account, AccountSerializer, Mission}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController
import queries.CreateAccount
import scalikejdbc._

class Accounts @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import Responses._
  import json4s._
  import json4s.implicits._

  implicit val formats = DefaultFormats + AccountSerializer

  def show() = Action { implicit req =>
    withAuth(NormalUser) { user =>
      Ok(Extraction.decompose(user))
    }
  }

  def showMin(id: Long) = Action {
    Account.findById(id).fold(notFound("player")) { account =>
      Ok(Extraction.decompose(account.minimal))
    }
  }

  def createAccount() = Action(json) { req =>
    req.body.extractOpt[CreateAccount].fold(JSONParseError) { account =>
      account.account.save()(AutoSession)
      Success
    }
  }

  def missions(id: Long) = Action {
    import models.DefaultAliases.m
    val missions = Mission.findAllBy(sqls.eq(m.creator, id))
    Ok(Extraction.decompose(missions.sortBy(-_.rate)))
  }

}
