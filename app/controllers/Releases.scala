package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import models.{Diagram, TrainTypeSerializer}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController
import responses.{DiagramResponse, ReleaseResponse}
import scalikejdbc._

class Releases @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import json4s.implicits._

  implicit val format = DefaultFormats + TrainTypeSerializer

  def list() = Action { implicit req =>
    import models.DefaultAliases.d
    withAuth(Administrator) { _ =>
      val diagrams = Diagram.joins(Diagram.stopStationRef).findAllBy(sqls.isNotNull(d.staging))
      val result = diagrams.groupBy(_.staging.get).map {
        case (staging, ds) =>
          ReleaseResponse(staging, ds.map(DiagramResponse.fromDiagram))
      }
      Ok(Extraction.decompose(result))
    }
  }
}
