package controllers

import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import models.{LineStation, Station}
import org.json4s._
import play.api.mvc.{Action, Controller}
import queries.CreateLine
import scalikejdbc._

class Lines @Inject()(json4s: Json4s) extends Controller {
  import Responses._
  import json4s._
  implicit val formats = DefaultFormats

  def create() = Action(json) { req =>
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
}
