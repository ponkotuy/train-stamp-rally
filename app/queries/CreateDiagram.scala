package queries

import models.{Diagram, StopStation, Train, TrainType}
import utils.TrainTime

final case class CreateDiagram(
    name: String,
    trainType: Int,
    subType: String,
    starts: Seq[String],
    stops: Seq[CreateStopStation]
) {
  def diagram(staging: Option[Long]) = Diagram(0L, name, trainTypeOpt.getOrElse(TrainType.Local), subType, staging)
  def trains(diagramId: Long) =
    starts.flatMap(TrainTime.fromString).map { time => Train(0L, diagramId, time) }
  def trainTypeOpt: Option[TrainType] = TrainType.find(trainType)
}

final case class CreateStopStation(lineStationId: Long, arrival: Option[Int], departure: Option[Int]) {
  def stopStation(diagramId: Long) = StopStation(0L, diagramId, lineStationId, arrival, departure)
}
