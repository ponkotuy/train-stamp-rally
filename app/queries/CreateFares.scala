package queries

import models.{Fare, TrainType}

final case class CreateFares(fares: Seq[CreateFare])

final case class CreateFare(km: Double, cost: Int) {
  def fare(companyId: Long, trainType: TrainType) = Fare(0L, companyId, trainType, km, cost)
}
