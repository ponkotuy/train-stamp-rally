package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyNoIdCRUDMapper}

case class MissionRate(missionId: Long, rate: Int)

object MissionRate extends SkinnyNoIdCRUDMapper[MissionRate] {
  override def defaultAlias: Alias[MissionRate] = createAlias("mr")

  override def extract(rs: WrappedResultSet, n: ResultName[MissionRate]): MissionRate = autoConstruct(rs, n)

  def upsert(missionId: Long, rate: Int)(implicit session: DBSession): Unit = {
    sql"INSERT INTO ${table} (mission_id, rate) VALUES (${missionId}, ${rate}) ON DUPLICATE KEY UPDATE rate = rate + ${rate}"
        .update().apply()
  }
}
