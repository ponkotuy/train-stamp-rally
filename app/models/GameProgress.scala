package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyNoIdCRUDMapper}
import utils.MissionTime

case class GameProgress(gameId: Long, stationId: Long, arrivalTime: Option[MissionTime]) {
  def save()(implicit session: DBSession): Unit = GameProgress.save(this)
}

object GameProgress extends SkinnyNoIdCRUDMapper[GameProgress] {
  override def defaultAlias: Alias[GameProgress] = createAlias("gp")

  override def extract(rs: WrappedResultSet, n: ResultName[GameProgress]): GameProgress = autoConstruct(rs, n)

  def save(gp: GameProgress)(implicit session: DBSession): Unit =
    createWithAttributes(
      'gameId -> gp.gameId,
      'stationId -> gp.stationId,
      'arrivalTime -> gp.arrivalTime.map(_.toString)
    )
}