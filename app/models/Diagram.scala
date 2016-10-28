package models

import responses.DiagramTrait
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.TrainTime

case class Diagram(
    id: Long,
    name: String,
    trainType: TrainType,
    subType: String,
    stops: Seq[StopStation] = Nil,
    trains: Seq[Train] = Nil
) extends DiagramTrait {
  def save()(implicit session: DBSession): Long = Diagram.save(this)
  def update()(implicit session: DBSession): Int = Diagram.update(this)

  def nextTrain(lineStationId: Long, time: TrainTime): Option[Train] = {
    for {
      stop <- stops.find(_.lineStationId == lineStationId)
      dep <- stop.departure
      train <- trains.find(time <= _.start.addMinutes(dep)).orElse(trains.headOption)
    } yield train
  }
}

object Diagram extends SkinnyCRUDMapperWithId[Long, Diagram] {
  override def defaultAlias: Alias[Diagram] = createAlias("d")

  override def extract(rs: WrappedResultSet, n: ResultName[Diagram]): Diagram = autoConstruct(rs, n, "stops", "trains")

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  lazy val stopStationRef = hasMany[StopStation](
    many = StopStation -> StopStation.defaultAlias,
    on = (d, ss) => sqls.eq(d.id, ss.diagramId),
    merge = (d, sss) => d.copy(stops = sss)
  )

  lazy val trainRef = hasMany[Train](
    many = Train -> Train.defaultAlias,
    on = (d, t) => sqls.eq(d.id, t.diagramId),
    merge = (d, ts) => d.copy(trains = ts.sortBy(_.start))
  )

  def save(d: Diagram)(implicit session: DBSession): Long =
    createWithAttributes(params(d):_*)

  def update(d: Diagram)(implicit session: DBSession): Int =
    updateById(d.id).withAttributes(params(d):_*)

  def params(d: Diagram) = Seq(
    'name -> d.name,
    'trainType -> d.trainType.value,
    'subType -> d.subType
  )

  override val defaultOrderings = Seq(column.id)
}
