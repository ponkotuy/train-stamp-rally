package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import models.GameProgress
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController
import scalikejdbc._

class GameProgresses @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import json4s.implicits._

  implicit val format = DefaultFormats

  def list(gameId: Long) = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      val gp = GameProgress.defaultAlias
      val progresses = GameProgress.joins(GameProgress.stationRef).findAllBy(sqls.eq(gp.gameId, gameId))
      Ok(Extraction.decompose(progresses))
    }
  }
}
