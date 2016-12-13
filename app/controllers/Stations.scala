package controllers

import authes.AuthConfigImpl
import authes.Role.{Administrator, NormalUser}
import caches.LineStationsCache
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{LineStation, Station, StationRankSerializer}
import org.json4s._
import play.api.mvc.Controller
import queries.CreateStationImpl
import scalikejdbc._

class Stations @Inject() (json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._
  implicit val formats = DefaultFormats + StationRankSerializer

  def list(q: Option[String]) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.DefaultAliases.s
    val where = q.map { name => sqls.like(s.name, s"%${name}").or.like(s.name, s"${name}%") }.getOrElse(sqls"true")
    Ok(Extraction.decompose(Station.findAllBy(where, Seq(s.id))))
  }

  def update(stationId: Long) = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateStationImpl].fold(JSONParseError) { station =>
      station.station.fold(notFound(s"rankValue: ${station.rankValue}")) { s =>
        DB localTx { implicit session =>
          if (s.copy(id = stationId).update() == 0) notFound(s"station id=${stationId}")
          else Success
        }
      }
    }
  }

  def show(stationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(Station.findById(stationId)))
  }

  def lines(stationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.DefaultAliases.ls
    val lineStations = LineStation.joins(LineStation.lineRef).findAllBy(sqls.eq(ls.stationId, stationId))
    Ok(Extraction.decompose(lineStations))
  }

  def lineStationList() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(LineStationsCache())
  }

  def lineStation(lineStationId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(LineStation.joins(LineStation.stationRef).findById(lineStationId)))
  }

  def replace(lineStationId: Long) = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    import scalikejdbc.TxBoundary.Either._
    req.body.extractOpt[CreateStationImpl].fold(JSONParseError) { station =>
      station.station.fold(notFound(s"rankValue: ${station.rankValue}")) { s =>
        val result = DB localTx { implicit session =>
          val id = s.save()
          if (LineStation.updateById(lineStationId).withAttributes('stationId -> id) == 0)
            Left(notFound(s"lineStation id=${lineStationId}"))
          else Right(Success)
        }
        result.merge
      }
    }
  }
}
