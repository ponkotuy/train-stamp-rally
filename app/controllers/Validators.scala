package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Diagram, LineStation}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import responses.DiagramResponse
import scalikejdbc.AutoSession
import validator.{DiagramValidator, ErrorSerializer, StationStopValidator}

class Validators @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._

  implicit val format = DefaultFormats + ErrorSerializer

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    import Diagram.{stopStationRef, trainRef}
    import LineStation.{lineRef, stationRef}
    val diagrams = Diagram.joins(trainRef, stopStationRef).findAll()(AutoSession)
    val diagramsWith = diagrams.map(DiagramResponse.fromDiagram)
    val stations = LineStation.joins(lineRef, stationRef).findAll()
    val errors =
      diagramsWith.flatMap(DiagramValidator.validate) ++
          new StationStopValidator(diagrams).validate(stations)
    Ok(Extraction.decompose(errors))
  }
}
