package controllers

import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import models.{Diagram, TrainType, TrainTypeSerializer}
import org.json4s._
import play.api.mvc.{Action, Controller}
import queries.CreateDiagram
import scalikejdbc.DB

class Diagrams @Inject()(json4s: Json4s) extends Controller {
  import Responses._
  import json4s._
  implicit val formats = DefaultFormats + new TrainTypeSerializer

  def list() = Action {
    Ok(Extraction.decompose(Diagram.findAll(Seq(Diagram.column.id))))
  }

  def create() = Action(json) { req =>
    req.body.extractOpt[CreateDiagram].fold(JSONParseError) { diagram =>
      createDiagram(diagram)
      Success
    }
  }

  def trainTypes() = Action {
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
