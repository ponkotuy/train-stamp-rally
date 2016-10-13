package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.MissionTime

case class Score(
    id: Long,
    missionId: Long,
    accountId: Long,
    time: MissionTime,
    distance: Double,
    money: Int,
    created: Long
) {
  def save()(implicit session: DBSession): Long = Score.save(this)
}

object Score extends SkinnyCRUDMapperWithId[Long, Score] {
  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  override def defaultAlias: Alias[Score] = createAlias("sc")

  override def extract(rs: WrappedResultSet, n: ResultName[Score]): Score = autoConstruct(rs, n)

  def save(score: Score)(implicit session: DBSession): Long =
    createWithAttributes(
      'missionId -> score.missionId,
      'accountID -> score.accountId,
      'time -> score.time.toString,
      'money -> score.money,
      'distance -> score.distance,
      'created -> score.created
    )
}
