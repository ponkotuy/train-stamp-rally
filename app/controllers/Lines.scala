package controllers

import authes.AuthConfigImpl
import authes.Role.{Administrator, NormalUser}
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Line, LineStation, Station, StationRankSerializer}
import org.json4s._
import play.api.mvc.Controller
import queries.CreateLine
import scalikejdbc._

class Lines @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._
  implicit val formats = DefaultFormats + StationRankSerializer

  def list() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok(Extraction.decompose(Line.findAll(Seq(Line.defaultAlias.id))))
  }

  def create() = StackAction(json, AuthorityKey -> Administrator) { implicit req =>
    req.body.extractOpt[CreateLine].fold(JSONParseError) { line =>
      createLine(line)
      Success
    }
  }

  private[this] def createLine(line: CreateLine): Long = {
    DB localTx { implicit session =>
      val lineId = line.line.save()
      val stations = line.stations
          .flatMap { st => st.station.map(st -> _) }
          .map { case (key, st) => key -> upsertStation(st) }
      stations.foreach { case (create, stId) =>
        LineStation(0L, lineId, stId, create.km).save()
      }
      lineId
    }
  }

  private[this] def upsertStation(st: Station)(implicit session: DBSession): Long =
    Station.findByName(st.name).fold(st.save())(_.id)

  def lineStations(lineId: Long) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import LineStation._
    val stations = joins(stationRef).findAllBy(sqls.eq(defaultAlias.lineId, lineId))
    Ok(Extraction.decompose(stations))
  }
}
