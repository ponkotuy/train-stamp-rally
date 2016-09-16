package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyJoinTable, SkinnyNoIdCRUDMapper}

case class MissionStation(missionId: Long, stationId: Long) {
  def save()(implicit session: DBSession): Unit = MissionStation.save(this)
}

object MissionStation extends SkinnyNoIdCRUDMapper[MissionStation] with SkinnyJoinTable[MissionStation] {
  override def defaultAlias: Alias[MissionStation] = createAlias("ms")
  override def extract(rs: WrappedResultSet, n: ResultName[MissionStation]): MissionStation = autoConstruct(rs, n)

  def save(ms: MissionStation)(implicit session: DBSession): Unit =
    createWithAttributes('missionId -> ms.missionId, 'stationId -> ms.stationId)
}
