package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyNoIdCRUDMapper}
import utils.MissionTime

case class GameProgress(
    gameId: Long,
    stationId: Long,
    arrivalTime: Option[MissionTime],
    station: Option[Station] = None
) {
  def save()(implicit session: DBSession): Unit = GameProgress.save(this)

  def update()(implicit session: DBSession): Unit = GameProgress.update(this)
}

object GameProgress extends SkinnyNoIdCRUDMapper[GameProgress] {
  override val defaultAlias: Alias[GameProgress] = createAlias("gp")

  override def extract(rs: WrappedResultSet, n: ResultName[GameProgress]): GameProgress = autoConstruct(rs, n, "station")

  lazy val stationRef = belongsTo[Station](
    right = Station,
    merge = (gp, s) => gp.copy(station = s)
  )

  def save(gp: GameProgress)(implicit session: DBSession): Unit =
    createWithAttributes(
      'gameId -> gp.gameId,
      'stationId -> gp.stationId,
      'arrivalTime -> gp.arrivalTime.map(_.toString)
    )

  def update(gp: GameProgress)(implicit session: DBSession): Unit =
    updateBy(sqls.eq(column.gameId, gp.gameId).and.eq(column.stationId, gp.stationId))
      .withAttributes('arrivalTime -> gp.arrivalTime.map(_.toString))
}
