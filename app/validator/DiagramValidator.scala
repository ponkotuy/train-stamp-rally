package validator

import responses.{DiagramResponse, DiagramStationResponse}

object DiagramValidator {
  def validate(diagram: DiagramResponse): Seq[Error] = {
    if(diagram.stops.size < 2) { return List(new LackStopsError(diagram)) }
    diagram.stops.sliding(2).flatMap { case Seq(x, y) =>
      if(x.line.id != y.line.id && x.station.id != y.station.id) Some(new LineConnectionError(diagram, x, y))
      else None
    }.toSeq
  }

  abstract class DiagramError(diagram: DiagramResponse) extends Error {
    def content: String
    override def message: String =
      s"DiagramId = ${diagram.id}: ${content}"

    override def url: Option[String] = Some(s"/creator/diagram/index.html?edit=${diagram.id}")
  }

  class LineConnectionError(
      diagram: DiagramResponse,
      stop1: DiagramStationResponse,
      stop2: DiagramStationResponse) extends DiagramError(diagram) {
    override def content: String =
      s"Not connect from ${stop1.line.name}:${stop1.station.name} to ${stop2.line.name}:${stop2.station.name}"
  }

  class LackStopsError(diagram: DiagramResponse) extends DiagramError(diagram) {
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
