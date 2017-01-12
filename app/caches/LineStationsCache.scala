package caches

import models.{LineStation, StationRankSerializer}
import org.json4s.{Extraction, _}

object LineStationsCache extends HeapCache[JValue]() {
  import LineStation.{defaultAlias, lineRef, stationRef}
  implicit val formats: Formats = DefaultFormats + StationRankSerializer

  override def initializer(): JValue = {
    val all = LineStation.joins(lineRef, stationRef).findAll(Seq(defaultAlias.id))
    Extraction.decompose(all)
  }
}
