package queries

import models.{Mission, MissionStation}

case class CreateMission(name: String, stations: Seq[Long], startStation: Long, creator: Long) {
  def mission = Mission(0L, name, startStation, creator, System.currentTimeMillis())
  def missionStations(missionId: Long) = stations.map { s => MissionStation(missionId, s) }
}
