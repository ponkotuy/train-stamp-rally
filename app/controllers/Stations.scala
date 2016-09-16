package controllers

import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import models.{LineStation, Station, StationRankSerializer}
import org.json4s._
import play.api.mvc.{Action, Controller}

class Stations @Inject()(json4s: Json4s) extends Controller {
  import json4s._
  implicit val formats = DefaultFormats + StationRankSerializer

  def list() = Action {
    Ok(Extraction.decompose(Station.findAll(Seq(Station.column.id))))
  }

  def lineStationList() = Action {
    import LineStation.{defaultAlias, lineRef, stationRef}
    val all = LineStation.joins(lineRef).joins(stationRef).findAll(Seq(defaultAlias.id))
    Ok(Extraction.decompose(all))
  }
}
