package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyNoIdCRUDMapper}

final case class StationGeo(stationId: Long, latitude: Double, longitude: Double) {
  def save()(implicit session: DBSession) = StationGeo.create(this)
}

object StationGeo extends SkinnyNoIdCRUDMapper[StationGeo] {
  override val defaultAlias: Alias[StationGeo] = createAlias("sg")

  override def extract(rs: WrappedResultSet, n: ResultName[StationGeo]): StationGeo = autoConstruct(rs, n)

  def create(sg: StationGeo)(implicit session: DBSession) =
    createWithAttributes('stationId -> sg.stationId, 'latitude -> sg.latitude, 'longitude -> sg.longitude)
}
