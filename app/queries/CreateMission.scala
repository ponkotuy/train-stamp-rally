package queries

import models.{Mission, MissionStation}

case class CreateMission(name: String, stations: Seq[Long], startStation: Long) {
  def mission(creator: Long) = Mission(0L, name, startStation, creator, System.currentTimeMillis())
  def missionStations(missionId: Long) = stations.map { s => MissionStation(missionId, s) }
}
