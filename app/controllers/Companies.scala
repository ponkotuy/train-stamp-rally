package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.{Administrator, NormalUser}
import com.github.tototoshi.play2.json4s.Json4s
import models.{Company, Fare, TrainType, TrainTypeSerializer}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController
import queries.{CreateCompany, CreateFares}
import scalikejdbc._

class Companies @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import Responses._
  import json4s._
  import json4s.implicits._

  implicit val formats = DefaultFormats + TrainTypeSerializer

  def list() = Action { implicit req =>
    withAuth(Administrator) { _ =>
      Ok(Extraction.decompose(Company.findAll(Seq(Company.defaultAlias.id))))
    }
  }

  def create() = Action(json) { implicit req =>
    withAuth(Administrator) { _ =>
      req.body.extractOpt[CreateCompany].fold(JSONParseError) { company =>
        DB localTx { implicit session =>
          val id = company.company.save()
          Ok(id.toString)
        }
      }
    }
  }

  // Fare tableを持っている会社と料金の組み合わせ
  def existsFare() = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      Ok(Extraction.decompose(Fare.existsFare()(AutoSession)))
    }
  }

  def fares(companyId: Long, trainType: Int) = Action { implicit req =>
    import models.DefaultAliases.f
    withAuth(NormalUser) { _ =>
      TrainType.find(trainType).fold(notFound(s"train type: ${trainType}")) { tType =>
        val fares = Fare.findAllBy(sqls.eq(f.companyId, companyId).and.eq(f.trainType, tType.value))
        Ok(Extraction.decompose(fares))
      }
    }
  }

  def createFares(companyId: Long, trainType: Int) = Action(json) { implicit req =>
    withAuth(Administrator) { _ =>
      req.body.extractOpt[CreateFares].fold(JSONParseError) { fares =>
        TrainType.find(trainType).fold(notFound(s"train type: ${trainType}")) { tType =>
          DB localTx { implicit session =>
            val f = Fare.column
            Fare.deleteBy(sqls.eq(f.companyId, companyId).and.eq(f.trainType, trainType))
            fares.fares.map(_.fare(companyId, tType)).foreach(_.save)
          }
          Ok("Success")
        }
      }
    }
  }
}
