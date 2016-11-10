package games

import models.{Game, Station}
import responses.TrainResponse
import scalikejdbc.AutoSession
import utils.{FeeCalculator, TrainTime}

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
  def calc(train: TrainResponse, fromStation: Long, toStation: Long, companyId: Long): TrainBoardCost = {
    val distance = calcDistance(train, fromStation, toStation)
    val fee = FeeCalculator.calc(train.trainType, companyId, distance)(AutoSession)
    val station = train.stops.find(_.station.id == toStation).get
    val time = station.arrival.orElse(station.departure).get
    TrainBoardCost(distance, fee, time, station.station)
  }

  private def calcDistance(train: TrainResponse, fromStation: Long, toStation: Long): Double = {
    val (xs, ys) = train.stops.dropWhile(_.station.id != fromStation).span(_.station.id != toStation)
    (xs :+ ys.head).sliding(2).map { xs =>
      val Seq(x, y) = xs
      if (x.line.id == y.line.id) math.abs(x.lineStation.km - y.lineStation.km) else 0.0
    }.sum
  }
}
