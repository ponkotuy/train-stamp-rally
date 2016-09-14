package queries

import models.{Mission, MissionStation}

case class CreateMission(name: String, stations: Seq[Long]) {
  def mission = Mission(0L, name, System.currentTimeMillis())
  def missionStations(missionId: Long) = stations.map { s => MissionStation(missionId, s) }
}
