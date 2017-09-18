package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.{Administrator, NormalUser}
import caches.LineStationsCache
import com.github.tototoshi.play2.json4s.Json4s
import models.{Line, LineStation, Station, StationRankSerializer}
import org.json4s._
import play.api.mvc.{AbstractController, ControllerComponents}
import queries.{CreateLine, Paging}
import responses.{Page, WithPage}
import scalikejdbc._

import scala.concurrent.ExecutionContext

class Lines @Inject() (json4s: Json4s, _ec: ExecutionContext, cc: ControllerComponents) extends AbstractController(cc) with Authenticator {
  import Lines._
  import Responses._
  import json4s._
  import json4s.implicits._
  implicit val formats = DefaultFormats + StationRankSerializer
  implicit val ec = _ec

  val optionalPaging = parse.using { req =>
    if (req.getQueryString("page").isDefined) parse.form(Paging.form).map(Some(_))
    else parse.ignore(None)
  }

  def list() = Action(optionalPaging) { implicit req =>
    import models.DefaultAliases.l
    withAuth(NormalUser) { _ =>
      val result = req.body.fold[Any] {
        Line.findAll(Seq(l.id.asc))
      } { paging =>
        val where = paging.q.fold(sqls"true") { q => sqls.like(l.name, s"%${q}%") }
        val data = Line.findAllByWithPagination(where, paging.pagination, Seq(l.id.desc))
        val count = Line.countBy(where)
        WithPage(Page(count, paging.size, paging.page), data)
      }
      Ok(Extraction.decompose(result))
    }
  }

  def create() = Action(json) { implicit req =>
    withAuth(Administrator) { _ =>
      req.body.extractOpt[CreateLine].fold(JSONParseError) { line =>
        createLine(line)
        LineStationsCache.clear()
        Success
      }
    }
  }

  def lineStations(lineId: Long) = Action { implicit req =>
    import models.DefaultAliases.ls
    withAuth(NormalUser) { _ =>
      val stations = LineStation.joins(LineStation.stationRef).findAllBy(sqls.eq(ls.lineId, lineId), Seq(ls.km.asc))
      Ok(Extraction.decompose(stations))
    }
  }
}

object Lines {
  private def createLine(line: CreateLine): Long = {
    DB localTx { implicit session =>
      val lineId = line.line.save()
      val stations = line.stations
        .flatMap { st => st.station.map(st -> _) }
        .map { case (key, st) => key -> upsertStation(st) }
      stations.foreach {
        case (create, stId) =>
          LineStation(0L, lineId, stId, create.km).save()
      }
      lineId
    }
  }

  private[this] def upsertStation(st: Station)(implicit session: DBSession): Long =
    Station.findByName(st.name).fold(st.save())(_.id)
}
