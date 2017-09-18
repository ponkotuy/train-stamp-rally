package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.Json4s
import models._
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController
import queries._
import responses.{MinScore, MissionScore, Page, WithPage}
import scalikejdbc._

class Missions @Inject() (json4s: Json4s) extends InjectedController with Authenticator {
  import Missions._
  import Responses._
  import json4s._
  import json4s.implicits._

  implicit val formats = DefaultFormats + StationRankSerializer

  def show(missionId: Long) = Action {
    import models.DefaultAliases.sg
    val mission = Mission.joins(Mission.stationsRef, Mission.startStationRef).findById(missionId).map { m =>
      val ids = m.startStationId +: m.stations.map(_.id)
      val geos = StationGeo.findAllBy(sqls.in(sg.stationId, ids))
      val withGeos = m.stations.map { s => s.copy(geo = geos.find(_.stationId == s.id)) }
      val startWithGeo = m.startStation.map { s => s.copy(geo = geos.find(_.stationId == s.id)) }
      m.copy(stations = withGeos, startStation = startWithGeo)
    }
    Ok(Extraction.decompose(mission))
  }

  def list() = Action { implicit req =>
    withAuth(NormalUser) { user =>
      SearchMissions.form.bindFromRequest().fold(badRequest, search => {
        val missions = Mission.joins(Mission.stationsRef, Mission.startStationRef).findAllBy(search.where)
        val filter = search.filter()(AutoSession)
        val filtered = missions.filter(filter.apply).sortBy(-_.rate)
        val result = if (search.score) withScores(user.id, filtered)(AutoSession) else filtered
        val withPage = Paging.form.bindFromRequest().value.fold[Any](result) { p =>
          val total = result.size
          val data = result.slice(p.from, p.to)
          WithPage(Page(total, p.size, p.page), data)
        }
        Ok(Extraction.decompose(withPage))
      })
    }
  }

  def random(size: Int) = Action { implicit req =>
    withAuth(NormalUser) { _ =>
      val mission = RandomMission.create(size)(AutoSession)
      Ok(Extraction.decompose(mission))
    }
  }

  def create() = Action(json) { implicit req =>
    withAuth(NormalUser) { user =>
      req.body.extractOpt[CreateMission].fold(JSONParseError) { mission =>
        DB localTx { implicit session =>
          val missionId = mission.mission(user.id).save()
          mission.missionStations(missionId).foreach(_.save())
          Ok(missionId.toString)
        }
      }
    }
  }

  def clearCount(accountId: Long) = Action {
    Ok(Score.missionCount(accountId)(AutoSession).toString)
  }

  def update(id: Long) = Action(json) { implicit req =>
    withAuth(NormalUser) { user =>
      Mission.findById(id).flatMap { mission =>
        if (mission.creator != user.id) None
        else {
          req.body.extractOpt[UpdateMission].map { update =>
            Mission.updateById(id).withAttributes(update.attributes: _*)
            Success
          }
        }
      }.getOrElse(notFound(s"mission(id=$id"))
    }
  }

  def delete(id: Long) = Action { implicit req =>
    import MissionStation.{column => ms}
    withAuth(NormalUser) { user =>
      Mission.findById(id).flatMap { mission =>
        if (mission.creator != user.id) None
        else {
          MissionStation.deleteBy(sqls.eq(ms.missionId, id))
          if (Mission.deleteById(id) == 1) Some(Success) else None
        }
      }.getOrElse(notFound(s"mission(id=$id)"))
    }
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
