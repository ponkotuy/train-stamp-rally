package queries

import models.Line

case class CreateLine(name: String, stations: Seq[CreateLineStation], companyId: Long) {
  def line = Line(id = 0L, name = name, companyId = companyId)
}

case class CreateLineStation(name: String, km: Double, rankValue: Int) extends CreateStation
