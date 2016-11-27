package queries

import models.{Diagram, StopStation, Train, TrainType}
import utils.TrainTime

case class CreateDiagram(
    name: String,
    trainType: Int,
    subType: String,
    starts: Seq[String],
    stops: Seq[CreateStopStation]
) {
  def diagram(release: Option[Long]) = Diagram(0L, name, trainTypeOpt.getOrElse(TrainType.Local), subType, release)
  def trains(diagramId: Long) =
    starts.flatMap(TrainTime.fromString).map { time => Train(0L, diagramId, time) }
  def trainTypeOpt: Option[TrainType] = TrainType.find(trainType)
}

case class CreateStopStation(lineStationId: Long, arrival: Option[Int], departure: Option[Int]) {
  def stopStation(diagramId: Long) = StopStation(0L, diagramId, lineStationId, arrival, departure)
}
