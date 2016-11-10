package caches

import models.{LineStation, StationRankSerializer}
import org.json4s.{DefaultFormats, Extraction, _}

object LineStationsCache extends HeapCache[JValue]() {
  override def initializer(): JValue = {
    import LineStation.{defaultAlias, lineRef, stationRef}
    implicit val formats = DefaultFormats + StationRankSerializer
    val all = LineStation.joins(lineRef, stationRef).findAll(Seq(defaultAlias.id))
    Extraction.decompose(all)
  }
}
