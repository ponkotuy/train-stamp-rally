package validator

import models.{Diagram, LineStation}

import scala.collection.breakOut

// diagrams: 全diagramが必要. stop_stationがjoinされている必要がある
class StationStopValidator(diagrams: Seq[Diagram]) {
  import StationStopValidator._

  def validate(stations: Seq[LineStation]): Seq[Error] = {
    val stationIds: Set[Long] =
      diagrams.flatMap(_.stops.map(_.lineStationId))(breakOut)
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
