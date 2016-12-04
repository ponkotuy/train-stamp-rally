package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import games.TrainBoardCost
import jp.t2v.lab.play2.auth.AuthElement
import models._
import org.json4s._
import play.api.mvc.{Action, Controller, Result}
import queries.{Board, Clear}
import responses.TrainResponse
import scalikejdbc._
import utils.EitherUtil._

class Plays @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._

  implicit val format = DefaultFormats

  def board() = StackAction(json, AuthorityKey -> NormalUser) { implicit req =>
    import DefaultAliases.gp
    DB localTx { implicit session =>
      val result: Either[Result, Result] = for {
        b <- req.body.extractOpt[Board].toRight(JSONParseError)
        game <- Game.findBy(sqls.eq(Game.column.accountId, loggedIn.id).and.eq(Game.column.missionId, b.missionId))
            .toRight(notFound("Mission"))
        _ <- Either.cond(game.stationId == b.fromStation, Unit, BadRequest("Wrong fromStation."))
        train <- TrainResponse.fromTrainId(b.trainId).toRight(notFound("Train"))
        startLine <- train.stops.headOption.map(_.line).toRight(notFound("Stops"))
        companyId = startLine.companyId
        stopIds = train.stops.map(_.station.id)
        _ <- Either.cond(stopIds.contains(b.toStation) && stopIds.contains(b.fromStation), Unit, BadRequest("Wrong trainId."))
        _ <- Either.cond(stopIds.indexOf(b.fromStation) < stopIds.lastIndexOf(b.toStation), Unit, BadRequest("Wrong stations order."))
      } yield {
        val afterGame = TrainBoardCost.calc(train, b.fromStation, b.toStation, companyId).apply(game)
        val fixedGame: Game = GameProgress.findBy(sqls.eq(gp.gameId, game.id).and.eq(gp.stationId, b.toStation)).fold(afterGame) { progress =>
          if(progress.arrivalTime.isEmpty) {
            progress.copy(arrivalTime = Some(afterGame.time.addMinutes(-1))).update()
            afterGame.copy(time = afterGame.time.addMinutes(5)) // スタンプを押すのに5分
          } else afterGame
        }
        fixedGame.update()
        Success
      }
      result.merge
    }
  }

  def clear(missionId: Long) = StackAction(json, AuthorityKey -> NormalUser) { implicit req =>
    import DefaultAliases.{g, gp}
    DB localTx { implicit session =>
      val result = for {
        cl <- req.body.extractOpt[Clear].toRight(JSONParseError)
        _ <- Either.cond(cl.isValid, Unit, BadRequest("Invalid rate value"))
        game <- Game.findBy(sqls.eq(g.accountId, loggedIn.id).and.eq(g.missionId, missionId))
            .toRight(notFound("Mission"))
        progresses = GameProgress.findAllBy(sqls.eq(gp.gameId, game.id))
        _ <- Either.cond(progresses.forall(_.arrivalTime.isDefined), Unit, BadRequest("Not cleared."))
      } yield {
        game.score(cl.rate, System.currentTimeMillis()).save()
        Games.deleteGame(game)
        Success
      }
      result.merge
    }
  }

  def rankingTime(missionId: Long) = Action {
    Ok(Plays.ranking(RankingType.Time, missionId))
  }

  def rankingMoney(missionId: Long) = Action {
    Ok(Plays.ranking(RankingType.Money, missionId))
  }

  def rankingDistance(missionId: Long) = Action {
    Ok(Plays.ranking(RankingType.Distance, missionId))
  }
}

object Plays {
  implicit val format = DefaultFormats + AccountSerializer

  private def ranking(typ: RankingType, missionId: Long): JValue = {
    val sc = Score.column
    val scores = Score.ranking(typ.column, sqls.eq(sc.missionId, missionId), 20)(AutoSession)
    Extraction.decompose(scores)
  }
}

sealed abstract class RankingType(val column: SQLSyntax)

object RankingType {
  val sc = Score.column
  case object Time extends RankingType(sc.time)
  case object Money extends RankingType(sc.money)
  case object Distance extends RankingType(sc.distance)
}
