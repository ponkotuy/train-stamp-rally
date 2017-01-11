package games

import models.{Game, Station}
import responses.{DiagramResponse, TrainResponse}
import scalikejdbc.AutoSession
import utils.{FeeCalculator, TrainTime}

case class TrainBoardCost(distance: Double, fee: Int, time: TrainTime, station: Station, start: TrainTime) {
  def apply(game: Game): Game =
    game.copy(
      distance = game.distance + distance,
      money = game.money + fee,
      time = game.time.setTime(time, start < game.time.trainTime || time < game.time.trainTime),
      stationId = station.id,
      station = Some(station),
      updated = System.currentTimeMillis()
    )
}

object TrainBoardCost {
  def calc(train: TrainResponse, fromStation: Long, toStation: Long, companyId: Long): Option[TrainBoardCost] = {
    val distance = calcDistance(train, fromStation, toStation)
    for {
      fee <- FeeCalculator.calc(train.trainType, companyId, distance)(AutoSession)
      station <- train.stops.reverseIterator.find(_.station.id == toStation)
      time <- station.arrival.orElse(station.departure)
      start <- train.stops.find(_.station.id == fromStation).flatMap(_.departure)
    } yield TrainBoardCost(distance, fee, time, station.station, start)
  }

  private def calcDistance(train: TrainResponse, fromStation: Long, toStation: Long): Double = {
    val (xs, ys) = train.stops.dropWhile(_.station.id != fromStation).span(_.station.id != toStation)
    (xs :+ ys.head).sliding(2).map { xs =>
      val Seq(x, y) = xs
      if (x.line.id == y.line.id) math.abs(x.lineStation.km - y.lineStation.km) else 0.0
    }.sum
  }
}

case class TrainCost(station: Station, distance: Double, fee: Int)

object TrainCost {
  def calcDiagram(diagram: DiagramResponse, fromStation: Long): Seq[TrainCost] = {
    val distances = diagram.stops.find(_.station.id == fromStation).map { start =>
      val stops = diagram.stops.dropWhile(_.station.id != fromStation)
      val distances = stops.sliding(2).map { xs =>
        val Seq(x, y) = xs
        if (x.line.id == y.line.id) math.abs(x.lineStation.km - y.lineStation.km) else 0.0
      }.scanLeft(0.0)(_ + _)
      stops.zip(distances.toSeq)
    }.getOrElse(Nil)
    distances.flatMap {
      case (stop, distance) =>
        FeeCalculator.calc(diagram.trainType, diagram.stops.head.line.companyId, distance)(AutoSession)
          .map { fee => TrainCost(stop.station, distance, fee) }
    }
  }
}
