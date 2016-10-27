package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.Diagram
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import responses.DiagramResponse
import scalikejdbc.{AutoSession, DBSession}
import validator.{ErrorSerializer, DiagramValidator}

class Validators @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._

  implicit val format = DefaultFormats + ErrorSerializer

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    val errors = allDiagrams()(AutoSession).flatMap(DiagramValidator.validate)
    Ok(Extraction.decompose(errors))
  }

  private[this] def allDiagrams()(implicit session: DBSession) = {
    import Diagram.{trainRef, stopStationRef}
    val diagrams = Diagram.joins(trainRef, stopStationRef).findAll()
    diagrams.map(DiagramResponse.fromDiagram)
  }
}
