package controllers

import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import models.{Mission, StationRankSerializer}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.{CreateMission, RandomMission}
import scalikejdbc.{AutoSession, DB}

class Missions @Inject()(json4s: Json4s) extends Controller {
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats + StationRankSerializer

  def list() = Action {
    val missions = Mission.joins(Mission.stationsRef).joins(Mission.startStationRef).findAll(Seq(Mission.defaultAlias.id))
    Ok(Extraction.decompose(missions))
  }

  def random(size: Int) = Action {
    val mission = RandomMission.create(size)(AutoSession)
    Ok(Extraction.decompose(mission))
  }

  def create() = Action(json) { req =>
    req.body.extractOpt[CreateMission].fold(JSONParseError) { mission =>
      DB localTx { implicit session =>
        val missionId = mission.mission.save()
        mission.missionStations(missionId).foreach(_.save())
      }
      Success
    }
  }
}
