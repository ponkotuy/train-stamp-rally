package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.Game
import org.json4s.DefaultFormats
import play.api.mvc.{Controller, Result}
import queries.Board
import responses.TrainResponse
import scalikejdbc._
import utils.EitherUtil._
import utils.FeeCalculator

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
        stopIds = train.stops.map(_.id)
        _ <- Either.cond(stopIds.contains(b.toStation) && stopIds.contains(b.fromStation), Unit, BadRequest("Wrong trainId."))
        _ <- Either.cond(stopIds.indexOf(b.fromStation) < stopIds.indexOf(b.toStation), Unit, BadRequest("Wrong stations order."))
      } yield {
        val distance = train.stops.sliding(2).map { xs =>
          val Seq(x, y) = xs
          if (x.line.id == y.line.id) math.abs(x.lineStation.km - y.lineStation.km) else 0.0
        }.sum
        val fee = FeeCalculator.calc(train.trainType, distance)
        val station = train.stops.find(_.station.id == b.toStation).get
        val time = station.arrival.map(_.addMinutes(1)).orElse(station.departure).get
        game.copy(
          distance = game.distance + distance,
          money = game.money + fee,
          time = game.time.setTime(time)
        ).save()
        Success
      }
      result.merge
    }
  }
}
