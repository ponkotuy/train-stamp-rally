package queries

import models._
import play.api.data.Form
import play.api.data.Forms._
import responses.{DiagramTrait, DiagramWithTrain}
import scalikejdbc._
import utils.TrainTime

import scala.collection.breakOut

case class SearchDiagram(station: Option[Long], time: Option[String]) {
  def search()(implicit session: DBSession): Seq[DiagramTrait] = {
    station.fold(Diagram.findAll(): Seq[DiagramTrait]) { st =>
      val lineStations = LineStation.findAllBy(sqls.eq(LineStation.column.stationId, st))
      val stops = StopStation.findAllBy(sqls.in(StopStation.column.lineStationId, lineStations.map(_.id)))
      val diagramIds = stops.map(_.diagramId).distinct
      time.flatMap(TrainTime.fromString).fold(Diagram.joins(Diagram.stopStationRef).findAllByIds(diagramIds:_*): Seq[DiagramTrait]) { t =>
        val lineStationIdTable: Map[Long, Long] = stops.map { ls => ls.diagramId -> ls.lineStationId }(breakOut)
        val diagrams = Diagram.joins(Diagram.stopStationRef, Diagram.trainRef).findAllByIds(diagramIds:_*)
        diagrams.flatMap { d =>
          d.nextTrain(lineStationIdTable(d.id), t).map { train =>
            DiagramWithTrain(d.id, d.name, d.trainType, d.subType, d.stops, train)
          }
        }
      }
    }
  }
}

object SearchDiagram {
  val form = Form(
    mapping(
      "station" -> optional(longNumber(min = 0L)),
      "time" -> optional(text(minLength = 4, maxLength = 4))
    )(SearchDiagram.apply)(SearchDiagram.unapply)
  )
}
