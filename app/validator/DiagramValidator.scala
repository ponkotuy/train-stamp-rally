package validator

import models.{Diagram, LineStation, StopStation}

class DiagramValidator (allStops: Seq[StopStation], allLineStations: Seq[LineStation]) {
  def validate(diagram: Diagram): Seq[Error] = {
    val stops = allStops.filter(_.diagramId == diagram.id)
    if(stops.size < 2) { return List(new LackStopsError(diagram)) }
    stops.sliding(2).flatMap { case Seq(x, y) =>
      val lineStationX = allLineStations.find(_.id == x.lineStationId).get
      val lineStationY = allLineStations.find(_.id == y.lineStationId).get
      if(lineStationX.lineId != lineStationY.lineId &&
          lineStationX.stationId != lineStationY.stationId) Some(new LineConnectionError(diagram, x, y))
      else None
    }.toSeq
  }

  abstract class DiagramError(diagram: Diagram) extends Error {
    def content: String
    override def message: String =
      s"DiagramId = ${diagram.id}: ${content}"

    override def url: Option[String] = Some(s"/creator/diagram/index.html?edit=${diagram.id}")
  }

  class LineConnectionError(
      diagram: Diagram,
      stop1: StopStation,
      stop2: StopStation) extends DiagramError(diagram) {
    override def content: String =
      s"Not connect from ${stop1.lineStationId} to ${stop2.lineStationId}"
  }

  class LackStopsError(diagram: Diagram) extends DiagramError(diagram) {
    override def content: String = "Lack of stops"
  }
}

class LackTrainValidator(diagrams: Set[Long]) {
  def validate(diagramId: Long): Option[Error] =
    if(diagrams.contains(diagramId)) None else Some(new LackTrainsError(diagramId))
}

class LackTrainsError(diagramId: Long) extends Error {
  override def message: String = s"DiagramId = ${diagramId}: Lack of trains"

  override def url: Option[String] = Some(s"/creator/diagram/index.html?edit=${diagramId}")
}
