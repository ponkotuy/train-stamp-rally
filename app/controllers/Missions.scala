package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Mission, Score, StationGeo, StationRankSerializer}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.{CreateMission, Paging, RandomMission, SearchMissions}
import responses.{MinScore, MissionScore, Page, WithPage}
import scalikejdbc._

class Missions @Inject() (json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Missions._
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats + StationRankSerializer

  def show(missionId: Long) = Action {
    import models.DefaultAliases.sg
    val mission = Mission.joins(Mission.stationsRef, Mission.startStationRef).findById(missionId).map { m =>
      val ids = m.stations.map(_.id)
      val geos = StationGeo.findAllBy(sqls.in(sg.stationId, ids))
      val withGeos = m.stations.map { s => s.copy(geo = geos.find(_.stationId == s.id)) }
      m.copy(stations = withGeos)
    }
    Ok(Extraction.decompose(mission))
  }

  def list() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    SearchMissions.form.bindFromRequest().fold(badRequest, search => {
      val missions = Mission.joins(Mission.stationsRef, Mission.startStationRef).findAllBy(search.where)
      val filter = search.filter()(AutoSession)
      val filtered = missions.filter(filter.apply).sortBy(-_.rate)
      val result = if (search.score) withScores(loggedIn.id, filtered)(AutoSession) else filtered
      val withPage = Paging.form.bindFromRequest().value.fold[Any](result) { p =>
        val total = result.size
        val data = result.slice(p.from, p.to)
        WithPage(Page(total, p.size, p.page), data)
      }
      Ok(Extraction.decompose(withPage))
    })
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

  def clearCount(accountId: Long) = Action {
    Ok(Score.missionCount(accountId)(AutoSession).toString)
  }
}

object Missions {
  def withScores(accountId: Long, missions: Seq[Mission])(implicit session: DBSession): Seq[MissionScore] = {
    import models.DefaultAliases.sc
    val scores = Score.findAllBy(sqls.eq(sc.accountId, accountId).and.in(sc.missionId, missions.map(_.id)))
    missions.map { mission =>
      val xs = scores.filter(_.missionId == mission.id)
      if (xs.isEmpty) MissionScore(mission, None)
      else {
        val time = xs.map(_.time).min
        val distance = xs.map(_.distance).min
        val money = xs.map(_.money).min
        MissionScore(mission, Some(MinScore(time, distance, money)))
      }
    }
  }
}
