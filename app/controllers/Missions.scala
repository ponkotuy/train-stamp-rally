package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Mission, StationRankSerializer}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import queries.{CreateMission, RandomMission}
import scalikejdbc.{AutoSession, DB}

class Missions @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats + StationRankSerializer

  def list() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    val missions = Mission.joins(Mission.stationsRef).joins(Mission.startStationRef).findAll(Seq(Mission.defaultAlias.id))
    Ok(Extraction.decompose(missions))
  }

  def random(size: Int) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    val mission = RandomMission.create(size)(AutoSession)
    Ok(Extraction.decompose(mission))
  }

  def create() = StackAction(json, AuthorityKey -> NormalUser) { implicit req =>
    req.body.extractOpt[CreateMission].fold(JSONParseError) { mission =>
      DB localTx { implicit session =>
        val missionId = mission.mission.save()
        mission.missionStations(missionId).foreach(_.save())
      }
      Success
    }
  }
}
