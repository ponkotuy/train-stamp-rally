package responses

import models.Mission
import utils.MissionTime

case class MissionScore(mission: Mission, score: Option[MinScore])

case class MinScore(time: MissionTime, distance: Double, money: Int)
