package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.MissionTime

case class GameHistory(
    id: Long,
    gameId: Long,
    time: MissionTime,
    distance: Double,
    money: Int,
    stationId: Long,
    created: Long
) {
  def save()(implicit session: DBSession): Long = GameHistory.save(this)

  def revertGame(game: Game) =
    new Game(game.id, game.missionId, game.accountId, time, distance, money, stationId, game.created, System.currentTimeMillis())
}

object GameHistory extends SkinnyCRUDMapperWithId[Long, GameHistory] {
  override val defaultAlias: Alias[GameHistory] = createAlias("gh")

  override def extract(rs: WrappedResultSet, n: ResultName[GameHistory]): GameHistory = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id

  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(gh: GameHistory)(implicit session: DBSession): Long =
    createWithAttributes(
      'gameId -> gh.gameId,
      'time -> gh.time.toString,
      'distance -> gh.distance,
      'money -> gh.money,
      'station_id -> gh.stationId,
      'created -> gh.created
    )
}
