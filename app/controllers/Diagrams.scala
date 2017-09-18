package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.{Administrator, NormalUser}
import com.github.tototoshi.play2.json4s.Json4s
import games.TrainCost
import models._
import org.json4s._
import play.api.mvc.InjectedController
import queries.{CreateDiagram, SearchDiagram}
import responses.{DiagramResponse, TrainResponse}
import scalikejdbc._

class Diagrams @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import Diagrams._
  import Responses._
  import json4s._
  import json4s.implicits._

  implicit val formats = DefaultFormats + TrainTypeSerializer + StationRankSerializer

  def show(diagramId: Long) = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      val diagram = DiagramResponse.fromId(diagramId)(AutoSession)
      Ok(Extraction.decompose(diagram))
    }
  }

  def cost(diagramId: Long, from: Long) = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      DiagramResponse.fromId(diagramId)(AutoSession).fold(notFound(s"diagram id=${diagramId}")) { diagram =>
        Ok(Extraction.decompose(TrainCost.calcDiagram(diagram, from)))
      }
    }
  }

  def list() = Action(parse.form(SearchDiagram.form)) { req =>
    val diagrams = req.body.search()(AutoSession)
    Ok(Extraction.decompose(diagrams))
  }

  def create() = Action(json) { implicit req =>
    withAuth(Administrator) { user =>
      req.body.extractOpt[CreateDiagram].fold(JSONParseError) { diagram =>
        createDiagram(diagram, user.id)
        Success
      }
    }
  }

  def update(diagramId: Long) = Action(json) { implicit req =>
    withAuth(Administrator) { _ =>
      req.body.extractOpt[CreateDiagram].fold(JSONParseError) { diagram =>
        if (updateDiagram(diagramId, diagram) == 0) notFound(s"Diagram id=${diagramId}")
        else Success
      }
    }
  }

  def delete(diagramId: Long) = Action { implicit req =>
    withAuth(Administrator) { _ =>
      if (deleteDiagram(diagramId) <= 0) notFound("diagram") else Success
    }
  }

  def train(trainId: Long) = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      Ok(Extraction.decompose(TrainResponse.fromTrainId(trainId)(AutoSession)))
    }
  }

  def trainTypes() = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      Ok(Extraction.decompose(TrainType.values))
    }
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
