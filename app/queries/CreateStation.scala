package queries

import models.{Station, StationRank}

trait CreateStation {
  def name: String
  def rankValue: Int
  def rank: Option[StationRank] = StationRank.find(rankValue)
  def station: Option[Station] = rank.map { r =>
    Station(id = 0L, name = name, rank = r)
  }
}

case class CreateStationImpl(name: String, rankValue: Int) extends CreateStation
