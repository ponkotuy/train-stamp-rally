package validator

import models.Station

object StationGeoValidator {
  /**
   * @param stations: require joined geo
   */
  def validate(stations: Seq[Station]): Seq[Error] = {
    stations.flatMap { st =>
      if (st.geo.isEmpty) Some(new NotFoundGeo(st)) else None
    }
  }
}

class NotFoundGeo(station: Station) extends Error {
  override def message: String = s"Not found ${station.name} geo data. rank=${station.rank}"

  override def url: Option[String] = Some(s"/creator/station/index.html?id=${station.id}")
}
