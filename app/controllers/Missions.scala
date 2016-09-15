package controllers

import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import models.Mission
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.CreateMission
import scalikejdbc.DB

class Missions @Inject()(json4s: Json4s) extends Controller {
  import json4s._
  import Responses._

  implicit val formats = DefaultFormats

  def list() = Action {
    val missions = Mission.findAll(Seq(Mission.column.id))
    Ok(Extraction.decompose(missions))
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
