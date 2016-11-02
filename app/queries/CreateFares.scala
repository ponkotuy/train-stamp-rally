package queries

import models.{Fare, TrainType}

case class CreateFares(fares: Seq[CreateFare])

case class CreateFare(km: Double, cost: Int) {
  def fare(companyId: Long, trainType: TrainType) = Fare(0L, companyId, trainType, km, cost)
}
