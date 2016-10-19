package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Mission(
    id: Long,
    name: String,
    startStationId: Long,
    created: Long,
    stations: Seq[Station] = Nil,
    startStation: Option[Station] = None,
    rate: Int = 0
) {
  def save()(implicit session: DBSession): Long = Mission.save(this)
}

object Mission extends SkinnyCRUDMapperWithId[Long, Mission] {
  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  override def defaultAlias: Alias[Mission] = createAlias("m")

  override def extract(rs: WrappedResultSet, n: ResultName[Mission]): Mission = new Mission(
    id = rs.get(n.id),
    name = rs.get(n.name),
    startStationId = rs.get(n.startStationId),
    created = rs.get(n.created),
    rate = rs.get(MissionRate.defaultAlias.resultName.rate)
  )

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

  lazy val rateRef = hasOne[MissionRate](MissionRate, (m, mr) => m.copy(rate = mr.map(_.rate).getOrElse(0)))

  def save(mission: Mission)(implicit session: DBSession): Long =
    createWithAttributes('name -> mission.name, 'created -> mission.created, 'startStationId -> mission.startStationId)
}
