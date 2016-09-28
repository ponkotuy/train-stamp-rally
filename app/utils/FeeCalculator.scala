package utils

import models.TrainType

object FeeCalculator {
  def calc(typ: TrainType, distance: Double): Int = {
    default(distance.ceil.toInt)
  }

  def default(distance: Int): Int = {
    assert(distance > 0)
    if(distance <= 3) 140
    else if(distance <= 6) 190
    else if(distance <= 10) 200
    else if(distance <= 300) (distance * 1.62).ceil.toInt * 10
    else if(distance <= 600) (distance * 1.285 + 100.5).ceil.toInt * 10
    else math.ceil(distance * 0.705 + 448.5).toInt * 10
  }
}
