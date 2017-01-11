package queries
import models.{Mission, Station}
import play.api.data.Form
import play.api.data.Forms._
import scalikejdbc._

import scala.collection.breakOut

final case class SearchMissions(
    rank: Option[RankRate],
    score: Boolean,
    stationName: Option[String],
    name: Option[String],
    creator: Option[Long]
) {
  def filter()(implicit session: DBSession) = {
    import models.DefaultAliases.s
    val stations: Option[Set[Long]] = stationName.map { n =>
      Station.findAllBy(sqls.like(s.name, s"%${n}%")).map(_.id)(breakOut)
    }
    MissionFilter(rank, stations)
  }

  def where: SQLSyntax = {
    import models.DefaultAliases.m
    sqls.toAndConditionOpt(
      creator.map { creator => sqls.eq(m.creator, creator) },
      name.map { str => sqls.like(m.name, s"%${str}%") }
    ).getOrElse(sqls"true")
  }
}

final case class MissionFilter(rank: Option[RankRate], stations: Option[Set[Long]]) {
  def apply(x: Mission): Boolean = {
    rank.forall(RankRate.findSize(x.stations.size) == _) &&
      stations.forall { sts =>
        sts.contains(x.startStationId) ||
          x.stations.exists { st => sts.contains(st.id) }
      }
  }
}

object SearchMissions {
  val form = Form(
    mapping(
      "rank" -> optional(text(minLength = 1)
        .verifying(RankRate.constraint)
        .transform[RankRate](RankRate.find(_).getOrElse(RankRate.Easy), _.toString)),
      "score" -> optional(boolean).transform[Boolean](_.getOrElse(false), Some(_)),
      "station_name" -> optional(text(minLength = 1)),
      "name" -> optional(text(minLength = 1)),
      "creator" -> optional(longNumber(min = 1))
    )(SearchMissions.apply)(SearchMissions.unapply)
  )
}
