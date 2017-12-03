package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import models.{Diagram, History}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{InjectedController, Request, Result}
import queries.CreateHistory
import scalikejdbc._

class Histories @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import Histories.withETag
  import controllers.Responses._
  import json4s._
  import json4s.implicits._

  implicit val format = DefaultFormats

  def list() = Action { implicit req =>
    import models.DefaultAliases.h
    val lastId = History.lastId()(AutoSession).getOrElse(0L)
    withETag(lastId.toString) {
      val histories = History.findAllWithLimitOffset(limit = 20, orderings = Seq(h.id.desc))
      Ok(Extraction.decompose(histories))
    }
  }

  def create() = Action(json) { implicit req =>
    withAuth(Administrator) { _ =>
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

}

object Histories {
  import play.api.http.HeaderNames._
  import play.api.mvc.Results._

  def withETag(eTag: String)(content: => Result)(implicit req: Request[Any]): Result = {
    if (req.headers.get(IF_NONE_MATCH).contains(eTag)) {
      NotModified
    } else {
      content.withHeaders(ETAG -> s""""${eTag}"""")
    }
  }
}
