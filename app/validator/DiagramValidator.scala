package validator

import models.{LineStation, StopStation}

class DiagramValidator(allStops: Seq[StopStation], allLineStations: Seq[LineStation]) {
  import DiagramValidator._
  def validate(diagramId: Long): Seq[Error] = {
    val stops = allStops.filter(_.diagramId == diagramId)
    if (stops.size < 2) { return List(new LackStopsError(diagramId)) }
    if (!checkStart(stops.head)) return List(new StartStopError(diagramId, stops.head))
    if (!checkEnd(stops.last)) return List(new EndStopError(diagramId, stops.last))
    stops.sliding(2).flatMap {
      case Seq(x, y) =>
        val lineStationX = allLineStations.find(_.id == x.lineStationId).get
        val lineStationY = allLineStations.find(_.id == y.lineStationId).get
        if (lineStationX.lineId != lineStationY.lineId &&
          lineStationX.stationId != lineStationY.stationId) Some(new LineConnectionError(diagramId, x, y))
        else None
    }.toSeq
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

class LackTrainValidator(diagrams: Set[Long]) {
  def validate(diagramId: Long): Option[Error] =
    if (diagrams.contains(diagramId)) None else Some(new LackTrainsError(diagramId))
}

class LackTrainsError(diagramId: Long) extends Error {
  override def message: String = s"DiagramId = ${diagramId}: Lack of trains"

  override def url: Option[String] = Some(s"/creator/diagram/index.html?edit=${diagramId}")
}
