package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import games.TrainBoardCost
import jp.t2v.lab.play2.auth.AuthElement
import models.{Game, GameProgress}
import org.json4s.DefaultFormats
import play.api.mvc.{Controller, Result}
import queries.Board
import responses.TrainResponse
import scalikejdbc._
import utils.EitherUtil._

class Plays @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
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

  def clear(missionId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    DB localTx { implicit session =>
      val g = Game.defaultAlias
      val result = for {
        game <- Game.findBy(sqls.eq(g.accountId, loggedIn.id).and.eq(g.missionId, missionId))
            .toRight(notFound("Mission"))
        gp = GameProgress.defaultAlias
        progresses = GameProgress.findAllBy(sqls.eq(gp.gameId, game.id))
        _ <- Either.cond(progresses.forall(_.arrivalTime.isDefined), Unit, BadRequest("Not cleared."))
      } yield {
        game.score(System.currentTimeMillis()).save()
        Games.deleteGame(game)
        Success
      }
      result.merge
    }
  }
}
