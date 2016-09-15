package queries

import models.{Station, StationRank}
import scalikejdbc._

import scala.util.Random
import scala.collection.breakOut

case class RandomMission(start: Station, stations: Seq[Station])

object RandomMission {
  import RankRate._

  val random = new Random

  // sizeは6の倍数が好ましい
  def create(size: Int)(implicit session: DBSession): RandomMission = {
    val start = findStations(StationRank.Top, 1).head
    val rates = if(size < 12) Easy()
      else if(size < 24) Medium()
      else Hard()
    val stations: Seq[Station] = rates.flatMap { case (rank, rate) =>
      findStations(rank, size / 6 * rate)
    }(breakOut)
    RandomMission(start, stations)
  }

  def findStations(rank: StationRank, size: Int)(implicit session: DBSession): Seq[Station] = {
    val s = Station.defaultAlias
    val stations = Station.findAllBy(sqls.eq(s.rank, rank.value))
    random.shuffle(stations).take(size)
  }
}

sealed abstract class RankRate(rates: Map[StationRank, Int]) {
  def apply() = rates
}

object RankRate {
  import StationRank._
  case object Easy extends RankRate(Map(
    Major -> 3,
    Large -> 2,
    Middle -> 1
  ))

  case object Medium extends RankRate(Map(
    Major -> 2,
    Large -> 2,
    Middle -> 1,
    Local -> 1
  ))

  case object Hard extends RankRate(Map(
    Major -> 1,
    Large -> 1,
    Middle -> 1,
    Local -> 3
  ))
}
