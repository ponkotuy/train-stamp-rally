package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import models.{Game, GameHistory, GameProgress, Mission}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{InjectedController, Result}
import scalikejdbc._
import utils.MissionTime

class Games @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import Games._
  import Responses._
  import json4s.implicits._

  implicit val formats = DefaultFormats

  def list() = Action { implicit req =>
    withAuth(NormalUser) { user =>
      val games = Game.findAllBy(sqls.eq(Game.column.accountId, user.id))
      Ok(Extraction.decompose(games))
    }
  }

  def show(missionId: Long) = Action { implicit req =>
    import models.DefaultAliases.g
    withAuth(NormalUser) { user =>
      val game = Game.joins(Game.stationRef)
        .findBy(sqls.eq(g.accountId, user.id).and.eq(g.missionId, missionId))
      Ok(Extraction.decompose(game))
    }
  }

  def create(missionId: Long) = Action { implicit req =>
    withAuth(NormalUser) { user =>
      DB localTx { implicit session =>
        val accountId = user.id
        val missionOpt = Mission.joins(Mission.stationsRef).findById(missionId)
        missionOpt.fold(NotFound("Not found missionId.")) { mission =>
          deleteGameIfExists(missionId, accountId)
          val now = System.currentTimeMillis()
          val gameId = Game(0L, missionId, accountId, MissionTime.Default, 0.0, 0, mission.startStationId, now, now).save()
          mission.stations.foreach { st => GameProgress(gameId, st.id, None).save() }
          Success
        }
      }
    }
  }

  def history(missionId: Long) = Action { implicit req =>
    import models.DefaultAliases.{g, gh}
    withAuth(NormalUser) { user =>
      val either = for {
        game <- Game.findBy(sqls.eq(g.missionId, missionId).and.eq(g.accountId, user.id)).toRight[Result](notFound("game"))
        history <- GameHistory.findAllByWithLimitOffset(
          sqls.eq(gh.gameId, game.id),
          limit = 1,
          offset = 0,
          Seq(gh.created.desc)
        ).headOption.toRight(notFound("history"))
      } yield {
        Ok(Extraction.decompose(history))
      }
      either.merge
    }
  }
}

object Games {
  def deleteGameIfExists(missionId: Long, accountId: Long)(implicit session: DBSession): Unit = {
    val g = Game.defaultAlias
    val gameOpt = Game.findBy(sqls.eq(g.missionId, missionId).and.eq(g.accountId, accountId))
    gameOpt.foreach(deleteGame)
  }

  def deleteGame(game: Game)(implicit session: DBSession): Unit = {
    GameProgress.deleteBy(sqls.eq(GameProgress.column.gameId, game.id))
    GameHistory.deleteBy(sqls.eq(GameHistory.column.gameId, game.id))
    Game.deleteById(game.id)
  }
}
