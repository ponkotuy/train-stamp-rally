package responses

import models._
import scalikejdbc.DBSession

final case class DiagramResponse(
  id: Long,
  name: String,
  trainType: TrainType,
  subType: String,
  stops: Seq[DiagramStationResponse],
  trains: Seq[Train]
)

object DiagramResponse {
  // trainRefとstopStationRefをjoinしている必要がある
  def fromDiagram(diagram: Diagram): DiagramResponse = {
    DiagramResponse(
      diagram.id,
      diagram.name,
      diagram.trainType,
      diagram.subType,
      diagram.stops.flatMap(DiagramStationResponse.fromStop),
      diagram.trains
    )
  }

  def fromId(id: Long)(implicit session: DBSession): Option[DiagramResponse] = {
    import Diagram.{trainRef, stopStationRef}
    Diagram.joins(trainRef, stopStationRef).findById(id).map(fromDiagram)
  }
}

final case class DiagramStationResponse(
  id: Long,
  arrival: Option[Int],
  departure: Option[Int],
  lineStation: LineStation,
  line: Line,
  station: Station
)

object DiagramStationResponse {
  def fromStop(stop: StopStation): Option[DiagramStationResponse] = {
    import LineStation.{lineRef, stationRef}
    val lineStation = LineStation.joins(lineRef, stationRef).findById(stop.lineStationId)
    for {
      ls <- lineStation
      l <- ls.line
      s <- ls.station
    } yield DiagramStationResponse(stop.id, stop.arrival, stop.departure, ls, l, s)
  }
}
