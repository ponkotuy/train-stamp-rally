package controllers

import authes.AuthConfigImpl
import authes.Role.{Administrator, NormalUser}
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Company, Fare, TrainType, TrainTypeSerializer}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import queries.{CreateCompany, CreateFares}
import scalikejdbc._

class Companies @Inject() (json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._
  implicit val formats = DefaultFormats + TrainTypeSerializer

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    Ok(Extraction.decompose(Company.findAll(Seq(Company.defaultAlias.id))))
  }

  def create() = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateCompany].fold(JSONParseError) { company =>
      DB localTx { implicit session =>
        val id = company.company.save()
        Ok(id.toString)
      }
    }
  }

  // Fare tableを持っている会社と料金の組み合わせ
  def existsFare() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(Fare.existsFare()(AutoSession)))
  }

  def fares(companyId: Long, trainType: Int) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.DefaultAliases.f
    TrainType.find(trainType).fold(notFound(s"train type: ${trainType}")) { tType =>
      val fares = Fare.findAllBy(sqls.eq(f.companyId, companyId).and.eq(f.trainType, tType.value))
      Ok(Extraction.decompose(fares))
    }
  }

  def createFares(companyId: Long, trainType: Int) = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
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
