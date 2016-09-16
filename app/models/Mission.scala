package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Mission(
    id: Long,
    name: String,
    startStationId: Long,
    created: Long,
    stations: Seq[Station] = Nil,
    startStation: Option[Station] = None
) {
  def save()(implicit session: DBSession): Long = Mission.save(this)
}

object Mission extends SkinnyCRUDMapperWithId[Long, Mission] {
  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  override def defaultAlias: Alias[Mission] = createAlias("m")

  override def extract(rs: WrappedResultSet, n: ResultName[Mission]): Mission =
    autoConstruct(rs, n, "stations", "startStation")

  lazy val stationsRef = hasManyThrough[MissionStation, Station](
    through = MissionStation -> MissionStation.defaultAlias,
    throughOn = (m, ms) => sqls.eq(m.id, ms.missionId),
    many = Station -> Station.createAlias("ss"),
    on = (ms, s) => sqls.eq(ms.stationId, s.id),
    merge = (m, sts) => m.copy(stations = sts)
  )


  lazy val startStationRef = belongsToWithFk[Station](
    right = Station,
    merge = (m, st) => m.copy(startStation = st),
    fk = "start_station_id"
  )

  def save(mission: Mission)(implicit session: DBSession): Long =
    createWithAttributes('name -> mission.name, 'created -> mission.created, 'startStationId -> mission.startStationId)
}
