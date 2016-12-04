package controllers

import authes.AuthConfigImpl
import authes.Role.{Administrator, NormalUser}
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import games.TrainCost
import jp.t2v.lab.play2.auth.AuthElement
import models._
import org.json4s._
import play.api.mvc.{Action, Controller}
import queries.{CreateDiagram, SearchDiagram}
import responses.{DiagramResponse, TrainResponse}
import scalikejdbc._

class Diagrams @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Diagrams._
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats + TrainTypeSerializer + StationRankSerializer

  def show(diagramId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    val diagram = DiagramResponse.fromId(diagramId)(AutoSession)
    Ok(Extraction.decompose(diagram))
  }

  def cost(diagramId: Long, from: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    DiagramResponse.fromId(diagramId)(AutoSession).fold(notFound(s"diagram id=${diagramId}")) { diagram =>
      Ok(Extraction.decompose(TrainCost.calcDiagram(diagram, from)))
    }
  }

  def list() = Action(parse.form(SearchDiagram.form)) { req =>
    val diagrams = req.body.search()(AutoSession)
    Ok(Extraction.decompose(diagrams))
  }

  def create() = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateDiagram].fold(JSONParseError) { diagram =>
      createDiagram(diagram, loggedIn.id)
      Success
    }
  }

  def update(diagramId: Long) = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateDiagram].fold(JSONParseError) { diagram =>
      if(updateDiagram(diagramId, diagram) == 0) notFound(s"Diagram id=${diagramId}")
      else Success
    }
  }

  def delete(diagramId: Long) = StackAction(AuthorityKey -> Administrator) { implicit req =>
    if(deleteDiagram(diagramId) <= 0) notFound("diagram") else Success
  }

  def train(trainId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(TrainResponse.fromTrainId(trainId)(AutoSession)))
  }

  def trainTypes() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(TrainType.values))
  }
}

object Diagrams {
  private def createDiagram(diagram: CreateDiagram, release: Long): Long = {
    DB localTx { implicit session =>
      val diagramId = diagram.diagram(Some(release)).save()
      diagram.trains(diagramId).foreach(_.save())
      diagram.stops.foreach { stop => stop.stopStation(diagramId).save() }
      diagramId
    }
  }

  private def updateDiagram(id: Long, diagram: CreateDiagram): Int = {
    DB localTx { implicit session =>
      Diagram.findById(id).map { db =>
        StopStation.deleteBy(sqls.eq(StopStation.column.diagramId, id))
        Train.deleteBy(sqls.eq(Train.column.diagramId, id))
        diagram.trains(id).foreach(_.save())
        diagram.stops.foreach(_.stopStation(id).save())
        diagram.diagram(db.staging).copy(id = id).update()
      }.getOrElse(0)
    }
  }

  private def deleteDiagram(id: Long): Int = {
    DB localTx { implicit session =>
      StopStation.deleteBy(sqls.eq(StopStation.column.diagramId, id))
      Train.deleteBy(sqls.eq(Train.column.diagramId, id))
      Diagram.deleteById(id)
    }
  }
}
