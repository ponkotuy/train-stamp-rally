package queries
import models.Mission
import play.api.data.Form
import play.api.data.Forms._

case class SearchMissions(rank: Option[RankRate], score: Boolean) {
  def filter(x: Mission): Boolean = rank.forall(RankRate.findSize(x.stations.size) == _)
}

object SearchMissions {
  val form = Form(
    mapping(
      "rank" -> optional(text(minLength = 1).verifying(RankRate.constraint).transform(RankRate.find(_).get, _.toString)),
      "score" -> optional(boolean).transform(_.getOrElse(false), Some(_))
    )(SearchMissions.apply)(SearchMissions.unapply)
  )
}
