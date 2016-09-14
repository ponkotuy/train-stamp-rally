package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyNoIdCRUDMapper}

case class MissionStation(missionId: Long, stationId: Long)

object MissionStation extends SkinnyNoIdCRUDMapper[MissionStation] {
  override def defaultAlias: Alias[MissionStation] = createAlias("ms")
  override def extract(rs: WrappedResultSet, n: ResultName[MissionStation]): MissionStation = autoConstruct(rs, n)
}
