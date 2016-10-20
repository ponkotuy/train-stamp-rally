package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Mission, StationRankSerializer}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import queries.{CreateMission, RandomMission, RankRate}
import scalikejdbc._

class Missions @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats + StationRankSerializer

  def list(rankStr: String) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    val rank = RankRate.find(rankStr)
    val missions = Mission.joins(Mission.stationsRef, Mission.startStationRef).findAll()
    val filtered = missions.filter { m =>
      val r = RankRate.findSize(m.stations.size)
      rank.forall(_ == r)
    }
    Ok(Extraction.decompose(filtered.sortBy(-_.rate)))
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
        Ok(missionId.toString)
      }
    }
  }
}
