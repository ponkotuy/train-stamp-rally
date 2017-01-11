package responses

import models.{StopStation, Train, TrainType}

final case class DiagramWithTrain(
    id: Long,
    name: String,
    trainType: TrainType,
    subType: String,
    stops: Seq[StopStation],
    train: Train
) extends DiagramTrait {
  override def trains: Seq[Train] = Seq(train)
}

trait DiagramTrait {
  def id: Long
  def name: String
  def trainType: TrainType
  def subType: String
  def stops: Seq[StopStation]
  def trains: Seq[Train]
}
