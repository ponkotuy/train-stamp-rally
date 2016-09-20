package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Game, GameProgress, Mission}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import scalikejdbc._
import utils.MissionTime

class Games @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats

  def list() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    val games = Game.findAllBy(sqls.eq(Game.column.accountId, loggedIn.id))
    Ok(Extraction.decompose(games))
  }

  def create(missionId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    DB localTx { implicit session =>
      val accountId = loggedIn.id
      val missionOpt = Mission.joins(Mission.stationsRef).findById(missionId)
      missionOpt.fold(NotFound("Not found mission.")) { mission =>
        deleteGameIfExists(missionId, accountId)
        val gameId = Game(0L, missionId, accountId, MissionTime.Default, System.currentTimeMillis()).save()
        mission.stations.foreach { st => GameProgress(gameId, st.id, None).save() }
        Success
      }
    }
  }

  private[this] def deleteGameIfExists(missionId: Long, accountId: Long)(implicit session: DBSession): Unit = {
    val g = Game.defaultAlias
    val gameOpt = Game.findBy(sqls.eq(g.missionId, missionId).and.eq(g.accountId, accountId))
    gameOpt.foreach { game =>
      GameProgress.deleteBy(sqls.eq(GameProgress.column.gameId, game.id))
      Game.deleteById(game.id)
    }
  }
}