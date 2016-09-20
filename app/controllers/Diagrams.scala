package controllers

import authes.AuthConfigImpl
import authes.Role.{Administrator, NormalUser}
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models._
import org.json4s._
import play.api.mvc.Controller
import queries.{CreateDiagram, SearchDiagram}
import scalikejdbc._

class Diagrams @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._
  implicit val formats = DefaultFormats + new TrainTypeSerializer

  def list() = StackAction(parse.form(SearchDiagram.form), AuthorityKey -> NormalUser) { implicit req =>
    val diagrams = req.body.search()(AutoSession)
    Ok(Extraction.decompose(diagrams))
  }

  def create() = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateDiagram].fold(JSONParseError) { diagram =>
      createDiagram(diagram)
      Success
    }
  }

  def trainTypes() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(TrainType.values))
  }

  private[this] def createDiagram(diagram: CreateDiagram): Long = {
    DB localTx { implicit session =>
      val diagramId = diagram.diagram.save()
      diagram.trains(diagramId).foreach(_.save())
      diagram.stops.foreach { stop => stop.stopStation(diagramId).save() }
      diagramId
    }
  }
}
