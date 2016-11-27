package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Diagram, TrainTypeSerializer}
import org.json4s.{DefaultFormats, Extraction}
import scalikejdbc._
import play.api.mvc.Controller
import responses.{DiagramResponse, ReleaseResponse}

class Releases @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._

  implicit val format = DefaultFormats + TrainTypeSerializer

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    import models.DefaultAliases.d
    val diagrams = Diagram.joins(Diagram.stopStationRef).findAllBy(sqls.isNotNull(d.staging))
    val result = diagrams.groupBy(_.staging.get).map { case (staging, ds) =>
      ReleaseResponse(staging, ds.map(DiagramResponse.fromDiagram))
    }
    Ok(Extraction.decompose(result))
  }
}
