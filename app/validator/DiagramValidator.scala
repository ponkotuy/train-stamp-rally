package validator

import models.{LineStation, StopStation}

class DiagramValidator(allStops: Seq[StopStation], allLineStations: Seq[LineStation]) {
  import DiagramValidator._
  def validate(diagramId: Long): Seq[Error] = {
    val stops = allStops.filter(_.diagramId == diagramId)
    if (stops.size < 2) List(new LackStopsError(diagramId))
    else if (!checkStart(stops.head)) List(new StartStopError(diagramId, stops.head))
    else if (!checkEnd(stops.last)) List(new EndStopError(diagramId, stops.last))
    else {
      stops.sliding(2).flatMap {
        case Seq(x, y) =>
          for {
            lineStationX <- allLineStations.find(_.id == x.lineStationId)
            lineStationY <- allLineStations.find(_.id == y.lineStationId)
            if lineStationX.lineId != lineStationY.lineId &&
              lineStationX.stationId != lineStationY.stationId
          } yield new LineConnectionError(diagramId, x, y)
      }.toSeq
    }
  }

  abstract class DiagramError(diagramId: Long) extends Error {
    def content: String
    override def message: String =
      s"DiagramId = ${diagramId}: ${content}"

    override def url: Option[String] = Some(s"/creator/diagram/index.html?edit=${diagramId}")
  }

  class LineConnectionError(
      diagramId: Long,
      stop1: StopStation,
      stop2: StopStation
  ) extends DiagramError(diagramId) {
    override def content: String =
      s"Not connect from ${stop1.lineStationId} to ${stop2.lineStationId}"
  }

  class LackStopsError(diagramId: Long) extends DiagramError(diagramId) {
    override val content: String = "Lack of stops"
  }

  class StartStopError(diagramId: Long, start: StopStation) extends DiagramError(diagramId) {
    override def content: String = s"Start stop validation error: ${start}"
  }

  class EndStopError(diagramId: Long, end: StopStation) extends DiagramError(diagramId) {
    override def content: String = s"End stop validation error: ${end}"
  }
}

object DiagramValidator {
  def checkStart(stop: StopStation): Boolean = stop.arrival.isEmpty && stop.departure == Some(0)
  def checkEnd(stop: StopStation): Boolean = stop.departure.isEmpty && stop.arrival.isDefined
}
