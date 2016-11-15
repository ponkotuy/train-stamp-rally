package queries

import models.{Station, StationRank}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import scalikejdbc._

import scala.collection.breakOut
import scala.util.Random

case class RandomMission(name: String, start: Station, stations: Seq[Station])

object RandomMission {
  val random = new Random

  // sizeは6の倍数が好ましい
  def create(size: Int)(implicit session: DBSession): RandomMission = {
    val start = findStations(StationRank.Top, 1).head
    val rate = RankRate.findSize(size)
    val stations: Seq[Station] = rate().flatMap { case (rank, rate) =>
      findStations(rank, size / 6 * rate)
    }(breakOut)
    val name = s"${rate.name.capitalize} Mission"
    RandomMission(name, start, stations)
  }

  def findStations(rank: StationRank, size: Int)(implicit session: DBSession): Seq[Station] = {
    val s = Station.defaultAlias
    val stations = Station.findAllBy(sqls.eq(s.rank, rank.value))
    random.shuffle(stations).take(size)
  }
}

sealed abstract class RankRate(val name: String, val rates: Map[StationRank, Int]) {
  def apply() = rates
  def size: Int
}

object RankRate {
  import StationRank._
  case object Easy extends RankRate("easy", Map(
    Major -> 3,
    Large -> 2,
    Middle -> 1
  )) {
    override val size: Int = 12
  }

  case object Medium extends RankRate("medium", Map(
    Major -> 2,
    Large -> 2,
    Middle -> 1,
    Local -> 1
  )) {
    override val size: Int = 24
  }

  case object Hard extends RankRate("hard", Map(
    Major -> 1,
    Large -> 1,
    Middle -> 1,
    Local -> 3
  )) {
    override val size: Int = Int.MaxValue
  }

  val values = Seq(Easy, Medium, Hard)
  def find(name: String): Option[RankRate] = values.find(_.name == name)
  def findSize(size: Int): RankRate = values.find(size < _.size).getOrElse(Hard)

  val constraint = Constraint[String]("rankrate") { o =>
    val strs: Set[String] = values.map(_.name)(breakOut)
    if(strs.contains(o)) Valid else Invalid(ValidationError("rankrate.value", o))
  }
}
