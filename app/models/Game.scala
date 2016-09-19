package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.MissionTime

case class Game(id: Long, missionId: Long, accountId: Long, time: MissionTime, created: Long) {
  def save()(implicit session: DBSession): Long = Game.save(this)
}

object Game extends SkinnyCRUDMapperWithId[Long, Game] {
  override def defaultAlias: Alias[Game] = createAlias("g")

  override def extract(rs: WrappedResultSet, n: ResultName[Game]): Game = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(game: Game)(implicit session: DBSession): Long =
    createWithAttributes(
      'missionId -> game.missionId,
      'accountId -> game.accountId,
      'time -> game.time.toString,
      'created -> game.created
    )
}
