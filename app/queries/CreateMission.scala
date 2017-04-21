package queries

import models.{Mission, MissionStation}

case class CreateMission(name: String, stations: Seq[Long], startStation: Long, introduction: String, clearText: String) {
  def mission(creator: Long) =
    Mission(0L, name, startStation, creator, System.currentTimeMillis(), introduction, clearText)
  def missionStations(missionId: Long) = stations.map { s => MissionStation(missionId, s) }
}
