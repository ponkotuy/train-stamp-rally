package games

import models.{Game, Station}
import utils.TrainTime

/**
  * Created by yosuke on 16/10/13.
  */
case class TrainBoardCost(distance: Double, fee: Int, time: TrainTime, station: Station) {
  def apply(game: Game): Game =
    game.copy(
      distance = game.distance + distance,
      money = game.money + fee,
      time = game.time.setTime(time),
      stationId = station.id,
      station = Some(station),
      updated = System.currentTimeMillis()
    )
}

object TrainBoardCost {
  def calc(train: TrainResponse, toStation: Long): TrainBoardCost = {
    val distance = calcDistance(train, toStation)
    val fee = FeeCalculator.calc(train.trainType, distance)
    val station = train.stops.find(_.station.id == toStation).get
    val time = station.arrival.map(_.addMinutes(1)).orElse(station.departure).get
    TrainBoardCost(distance, fee, time, station.station)
  }

  private def calcDistance(train: TrainResponse, toStation: Long): Double = {
    val (xs, ys) = train.stops.span(_.station.id != toStation)
    (xs :+ ys.head).sliding(2).map { xs =>
      val Seq(x, y) = xs
      if (x.line.id == y.line.id) math.abs(x.lineStation.km - y.lineStation.km) else 0.0
    }.sum
  }
}
