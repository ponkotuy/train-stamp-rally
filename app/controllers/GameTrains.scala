package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Game, GameProgress, Station}
import org.json4s.DefaultFormats
import play.api.mvc.{Controller, Result}
import queries.Board
import responses.TrainResponse
import scalikejdbc._
import utils.EitherUtil._
import utils.{FeeCalculator, TrainTime}

class GameTrains @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._

  implicit val format = DefaultFormats

  def board() = StackAction(json, AuthorityKey -> NormalUser) { implicit req =>
    DB localTx { implicit session =>
      val result: Either[Result, Result] = for {
        b <- req.body.extractOpt[Board].toRight(JSONParseError)
        game <- Game.findBy(sqls.eq(Game.column.accountId, loggedIn.id).and.eq(Game.column.missionId, b.missionId))
            .toRight(notFound("Mission"))
        _ <- Either.cond(game.stationId == b.fromStation, Unit, BadRequest("Wrong fromStation."))
        train <- TrainResponse.fromTrainId(b.trainId).toRight(notFound("Train"))
        stopIds = train.stops.map(_.station.id)
        _ <- Either.cond(stopIds.contains(b.toStation) && stopIds.contains(b.fromStation), Unit, BadRequest("Wrong trainId."))
        _ <- Either.cond(stopIds.indexOf(b.fromStation) < stopIds.indexOf(b.toStation), Unit, BadRequest("Wrong stations order."))
      } yield {
        val afterGame = TrainBoardCost.calc(train, b.toStation).apply(game)
        afterGame.update()
        val gp = GameProgress.defaultAlias
        GameProgress.findBy(sqls.eq(gp.gameId, game.id).and.eq(gp.stationId, b.toStation)).foreach { progress =>
          if(progress.arrivalTime.isEmpty) {
            progress.copy(arrivalTime = Some(afterGame.time.addMinutes(-1))).update()
          }
        }
        Success
      }
      result.merge
    }
  }
}

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
