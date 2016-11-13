package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Diagram, LineStation, Train}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import responses.DiagramResponse
import scalikejdbc.AutoSession
import utils.MethodProfiler
import validator.{DiagramValidator, ErrorSerializer, LackTrainValidator, StationStopValidator}

class Validators @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._

  implicit val format = DefaultFormats + ErrorSerializer

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    import Diagram.stopStationRef
    import LineStation.{lineRef, stationRef}
    val profiler = MethodProfiler.apply()
    val errors = profiler("all") {
      val diagrams = profiler("diagrams") {
        Diagram.joins(stopStationRef).findAll()(AutoSession)
      }
      val trains = profiler("trains") {
        Train.allDiagramIds()(AutoSession).toSet
      }
      val diagramsWith = profiler("diagramsResponses") {
        diagrams.map(DiagramResponse.fromDiagram)
      }
      val stations = profiler("lineStations") {
        LineStation.joins(lineRef, stationRef).findAll()
      }
      profiler("diagramsValidator") {
        diagramsWith.flatMap(DiagramValidator.validate)
      } ++ profiler("stationStopValidator") {
        new StationStopValidator(diagrams).validate(stations)
      } ++ profiler("lackTrainValidator") {
        val validator = new LackTrainValidator(trains)
        diagrams.flatMap { d => validator.validate(d.id) }
      }
    }
    Ok(Extraction.decompose(errors))
  }
}
