package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import caches.LineStationsCache
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{LineStation, Station, StationRankSerializer}
import org.json4s._
import play.api.mvc.Controller

class Stations @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._
  implicit val formats = DefaultFormats + StationRankSerializer

  def list() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(Station.findAll(Seq(Station.column.id))))
  }

  def show(stationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(Station.findById(stationId)))
  }

  def lineStationList() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(LineStationsCache())
  }

  def lineStation(lineStationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(LineStation.joins(LineStation.stationRef).findById(lineStationId)))
  }
}


