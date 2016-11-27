package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Diagram, History}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.CreateHistory
import scalikejdbc._

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
      DB.localTx { implicit session =>
        his.release.foreach { r =>
          Diagram.updateBy(sqls.eq(Diagram.column.staging, r)).withAttributes('staging -> None)
        }
        his.history().save()
        Success
      }
    }
  }
}
