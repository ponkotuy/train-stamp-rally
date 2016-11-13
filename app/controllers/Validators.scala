package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Diagram, LineStation, StopStation, Train}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import scalikejdbc.AutoSession
import utils.MethodProfiler
import validator.{DiagramValidator, ErrorSerializer, LackTrainValidator, StationStopValidator}

class Validators @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._

  implicit val format = DefaultFormats + ErrorSerializer

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    import LineStation.{lineRef, stationRef}
    val profiler = MethodProfiler.Nop
    val errors = profiler("all") {
      val diagrams = profiler("diagrams") {
        Diagram.findAllIds()(AutoSession)
      }
      val stops = profiler("stops") {
        StopStation.findAll()
      }
      val trains = profiler("trains") {
        Train.allDiagramIds()(AutoSession).toSet
      }
      val stations = profiler("lineStations") {
        LineStation.joins(lineRef, stationRef).findAll()
      }
      profiler("diagramsValidator") {
        val validator = new DiagramValidator(stops, stations)
        diagrams.flatMap(validator.validate)
      } ++ profiler("stationStopValidator") {
        new StationStopValidator(stops).validate(stations)
      } ++ profiler("lackTrainValidator") {
        val validator = new LackTrainValidator(trains)
        diagrams.flatMap(validator.validate)
      }
    }
    Ok(Extraction.decompose(errors))
  }
}
