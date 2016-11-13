package validator

import models.{LineStation, StopStation}

import scala.collection.breakOut

class StationStopValidator(allStops: Seq[StopStation]) {
  import StationStopValidator._

  def validate(stations: Seq[LineStation]): Seq[Error] = {
    val stationIds: Set[Long] = allStops.map(_.lineStationId)(breakOut)
    stations.flatMap { st =>
      if(stationIds.contains(st.id)) None
      else {
        Some(new UndefinedTrainError(st))
      }
    }
  }
}

object StationStopValidator {
  class UndefinedTrainError(station: LineStation) extends Error {
    override def message: String = s"Undefined arrived train at ${lineName}:${stationName}"
    override def url: Option[String] = None
    def lineName = station.line.map(_.name).getOrElse(s"(lineId = ${station.lineId})")
    def stationName = station.station.map(_.name).getOrElse(s"(stationId = ${station.stationId})")
  }
}
