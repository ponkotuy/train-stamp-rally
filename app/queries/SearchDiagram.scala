package queries

import models._
import play.api.data.Form
import play.api.data.Forms._
import responses.TrainResponse
import scalikejdbc._
import utils.TrainTime

import scala.collection.breakOut

sealed abstract class SearchDiagram {
  def search()(implicit session: DBSession): Any
  def tuple: (Option[Long], Option[String])
}

object SearchDiagram {
  case object All extends SearchDiagram {
    override def search()(implicit session: DBSession) = Diagram.findAll()
    override def tuple: (Option[Long], Option[String]) = (None, None)
  }

  case class StationSearch(stationId: Long) extends SearchDiagram {
    override def search()(implicit session: DBSession) = {
      val diagramIds = findDiagramIds(stationId)
      Diagram.joins(Diagram.stopStationRef).findAllByIds(diagramIds:_*)
    }

    override def tuple: (Option[Long], Option[String]) = (Some(stationId), None)
  }

  case class TimeSearch(stationId: Long, time: TrainTime) extends SearchDiagram {
    override def search()(implicit session: DBSession) = {
      val lineStations = LineStation.findAllBy(sqls.eq(LineStation.column.stationId, stationId))
      val stops = StopStation.findAllBy(sqls.in(StopStation.column.lineStationId, lineStations.map(_.id)))
      val diagramIds = stops.map(_.diagramId).distinct
      val lineStationIdTable: Map[Long, Long] = stops.map { ls => ls.diagramId -> ls.lineStationId }(breakOut)
      val diagrams = Diagram.joins(Diagram.stopStationRef, Diagram.trainRef).findAllByIds(diagramIds: _*)
      diagrams.flatMap { d =>
        d.nextTrain(lineStationIdTable(d.id), time).map { train =>
          TrainResponse.fromTrainDiagram(train, d)
        }
      }
    }

    override def tuple: (Option[Long], Option[String]) = (Some(stationId), Some(time.toString))
  }

  def apply(stationIdOpt: Option[Long], timeOpt: Option[String]): SearchDiagram = {
    stationIdOpt.fold(All: SearchDiagram) { stationId =>
      timeOpt.flatMap(TrainTime.fromString)
          .fold(StationSearch(stationId): SearchDiagram) { time => TimeSearch(stationId, time): SearchDiagram }
    }
  }

  def unapply(sd: SearchDiagram): Option[(Option[Long], Option[String])] = Some(sd.tuple)

  private[this] def findDiagramIds(stationId: Long)(implicit session: DBSession): Seq[Long] = {
    val lineStations = LineStation.findAllBy(sqls.eq(LineStation.column.stationId, stationId))
    val stops = StopStation.findAllBy(sqls.in(StopStation.column.lineStationId, lineStations.map(_.id)))
    stops.map(_.diagramId).distinct
  }

  val form = Form(
    mapping(
      "station" -> optional(longNumber(min = 0L)),
      "time" -> optional(text(minLength = 4, maxLength = 4))
    )(SearchDiagram.apply)(SearchDiagram.unapply)
  )
}
