package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.MissionTime

final case class Game(
    id: Long,
    missionId: Long,
    accountId: Long,
    time: MissionTime,
    distance: Double,
    money: Int,
    stationId: Long,
    created: Long,
    updated: Long,
    station: Option[Station] = None
) {
  def save()(implicit session: DBSession): Long = Game.save(this)

  def update()(implicit session: DBSession): Unit = Game.update(this)

  def score(rate: Int, created: Long) = Score(0L, missionId, accountId, time, distance, money, rate, created)

  def history(): GameHistory =
    new GameHistory(0L, id, time, distance, money, stationId, System.currentTimeMillis())
}

object Game extends SkinnyCRUDMapperWithId[Long, Game] {
  override val defaultAlias: Alias[Game] = createAlias("g")

  override def extract(rs: WrappedResultSet, n: ResultName[Game]): Game = autoConstruct(rs, n, "station")

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  lazy val stationRef = belongsTo[Station](
    right = Station,
    merge = (g, station) => g.copy(station = station)
  )

  def save(game: Game)(implicit session: DBSession): Long =
    createWithAttributes(
      'missionId -> game.missionId,
      'accountId -> game.accountId,
      'time -> game.time.toString,
      'distance -> game.distance,
      'money -> game.money,
      'station_id -> game.stationId,
      'created -> game.created,
      'updated -> game.created
    )

  // update time, distance, money, stationId
  def update(game: Game)(implicit session: DBSession): Boolean =
    updateById(game.id).withAttributes(
      'time -> game.time.toString,
      'distance -> game.distance,
      'money -> game.money,
      'stationId -> game.stationId,
      'updated -> game.updated
    ) > 0
}
