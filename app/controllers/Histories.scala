package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.History
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.CreateHistory
import scalikejdbc.AutoSession

class Histories @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import controllers.Responses._
  import json4s._

  implicit val format = DefaultFormats

  def list() = Action {
    import models.DefaultAliases.h
    Ok(Extraction.decompose(History.findAllWithLimitOffset(limit = 20, orderings = Seq(h.id.desc))))
  }

  def create() = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateHistory].fold(JSONParseError) { his =>
      his.history().save()(AutoSession)
      Success
    }
  }
}
