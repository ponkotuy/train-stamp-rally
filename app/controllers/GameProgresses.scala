package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.GameProgress
import org.json4s.{DefaultFormats, Extraction, Formats}
import play.api.mvc.Controller
import scalikejdbc._

class GameProgresses @Inject() (json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._

  implicit val format: Formats = DefaultFormats

  def list(gameId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    val gp = GameProgress.defaultAlias
    val progresses = GameProgress.joins(GameProgress.stationRef).findAllBy(sqls.eq(gp.gameId, gameId))
    Ok(Extraction.decompose(progresses))
  }
}
