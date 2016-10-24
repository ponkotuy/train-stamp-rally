package queries

import models._
import play.api.data.Form
import play.api.data.Forms._
import responses.{DiagramResponse, TrainResponse}
import scalikejdbc._
import skinny.Pagination
import utils.TrainTime

import scala.collection.breakOut

sealed abstract class SearchDiagram {
  def search()(implicit session: DBSession): Any
  def tuple: (Option[Long], Option[String], Option[Int], Option[Int])
}

object SearchDiagram {
  case class All(pageNo: Int, size: Int) extends SearchDiagram {
    override def search()(implicit session: DBSession): Seq[DiagramResponse] = {
      import Diagram.{trainRef, stopStationRef, defaultAlias => d}
      val page = Pagination.page(pageNo).per(size)
      Diagram.joins(trainRef, stopStationRef)
          .findAllWithPagination(page, Seq(d.id.desc))
          .map(DiagramResponse.fromDiagram)
    }

    override def tuple = (None, None, Some(pageNo), Some(size))
  }

  case class StationSearch(stationId: Long) extends SearchDiagram {
    override def search()(implicit session: DBSession) = {
      val diagramIds = findDiagramIds(stationId)
      Diagram.joins(Diagram.stopStationRef).findAllByIds(diagramIds:_*)
    }

    override def tuple = (Some(stationId), None, None, None)
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

    override def tuple  = (Some(stationId), Some(time.toString), None, None)
  }

  def apply(
      stationIdOpt: Option[Long],
      timeOpt: Option[String],
      pageNoOpt: Option[Int],
      sizeOpt: Option[Int]): SearchDiagram = {
    stationIdOpt.fold(All(pageNoOpt.getOrElse(1), sizeOpt.getOrElse(10)): SearchDiagram) { stationId =>
      timeOpt.flatMap(TrainTime.fromString)
          .fold(StationSearch(stationId): SearchDiagram) { time => TimeSearch(stationId, time): SearchDiagram }
    }
  }

  def unapply(sd: SearchDiagram): Option[(Option[Long], Option[String], Option[Int], Option[Int])] = Some(sd.tuple)

  private[this] def findDiagramIds(stationId: Long)(implicit session: DBSession): Seq[Long] = {
    val lineStations = LineStation.findAllBy(sqls.eq(LineStation.column.stationId, stationId))
    val stops = StopStation.findAllBy(sqls.in(StopStation.column.lineStationId, lineStations.map(_.id)))
    stops.map(_.diagramId).distinct
  }

  val form = Form(
    mapping(
      "station" -> optional(longNumber(min = 0L)),
      "time" -> optional(text(minLength = 4, maxLength = 4)),
      "page" -> optional(number),
      "size" -> optional(number)
    )(SearchDiagram.apply)(SearchDiagram.unapply)
  )
}
