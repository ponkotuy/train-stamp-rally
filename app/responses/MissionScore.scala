package responses

import models.Mission
import utils.MissionTime

final case class MissionScore(mission: Mission, score: Option[MinScore])

final case class MinScore(time: MissionTime, distance: Double, money: Int)
