package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import models._
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController
import scalikejdbc.AutoSession
import utils.MethodProfiler
import validator._

class Validators @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import json4s.implicits._
  implicit val format = DefaultFormats + ErrorSerializer

  def list() = Action { implicit req =>
    withAuth(Administrator) { _ =>
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
        } ++ profiler("stationGeoValidator") {
          val stations = Station.joins(Station.geoRef).findAll()
          StationGeoValidator.validate(stations)
        }
      }
      Ok(Extraction.decompose(errors))
    }
  }
}
