package responses

import models._
import scalikejdbc.DBSession
import utils.TrainTime

import scala.collection.breakOut

final case class TrainResponse(
  id: Long,
  start: TrainTime,
  diagramId: Long,
  name: String,
  trainType: TrainType,
  subType: String,
  stops: Seq[TrainStopResponse]
)

object TrainResponse {
  def fromTrainId(id: Long)(implicit session: DBSession): Option[TrainResponse] = {
    for {
      train <- Train.findById(id)
      diagram <- Diagram.joins(Diagram.stopStationRef).findById(train.diagramId)
    } yield fromTrainDiagram(train, diagram)
  }

  // diagramにはstopStationRefをjoinしたものが必要
  def fromTrainDiagram(train: Train, diagram: Diagram): TrainResponse = {
    val lineStationIds = diagram.stops.map(_.lineStationId)
    val lineStations: Map[Long, LineStation] = LineStation.joins(LineStation.stationRef, LineStation.lineRef)
      .findAllByIds(lineStationIds.distinct: _*)
      .map { ls => ls.id -> ls }(breakOut)
    val trainStops = diagram.stops.flatMap { stop =>
      lineStations.get(stop.lineStationId).flatMap { ls =>
        TrainStopResponse.fromObj(train, stop, ls)
      }
    }
    fromObj(train, diagram, trainStops)
  }

  def fromObj(train: Train, diagram: Diagram, stops: Seq[TrainStopResponse]): TrainResponse =
    TrainResponse(train.id, train.start, diagram.id, diagram.name, diagram.trainType, diagram.subType, stops)

}

final case class TrainStopResponse(
  id: Long,
  arrival: Option[TrainTime],
  departure: Option[TrainTime],
  lineStation: LineStation,
  line: Line,
  station: Station
)

object TrainStopResponse {
  def fromObj(train: Train, ss: StopStation, ls: LineStation): Option[TrainStopResponse] = {
    val arrival = ss.arrival.map(train.start.addMinutes)
    val departure = ss.departure.map(train.start.addMinutes)
    for {
      line <- ls.line
      station <- ls.station
    } yield TrainStopResponse(ss.id, arrival, departure, ls, line, station)
  }
}
