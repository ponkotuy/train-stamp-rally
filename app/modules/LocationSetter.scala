package modules

import javax.inject.{Inject, Singleton}

import com.google.maps.model.GeocodingResult
import models.{LineStation, StationGeo}
import play.api.{Configuration, Environment}
import play.api.inject.Module
import scalikejdbc._
import utils.{Config, GoogleMaps}

@Singleton
class LocationSetter @Inject() (config: Configuration, _p: PlayInitializer) {
  run()

  def run(): Unit = {
    new Thread(new LocationSetterThread(config)).start()
  }
}

object LocationSetter {
  val FindCount = 100
  val ReBranket = """\(.*\)""".r
  def normStationName(str: String) = ReBranket.replaceAllIn(str, "")

  def saveGeo(stationId: Long, geo: GeocodingResult)(implicit session: DBSession): Unit = {
    val loc = geo.geometry.location
    val sg = StationGeo(stationId, loc.lat, loc.lng)
    sg.save()(AutoSession)
  }

  def geoQuery(maps: GoogleMaps)(q: String): Option[GeocodingResult] = {
    val xs = maps.geocoding.request(q)
    if (xs.length == 1) Some(xs.head) else None
  }
}

class LocationSetterThread(config: Configuration) extends Runnable {
  import LocationSetter._
  val conf = new Config(config)
  val mapsOpt = conf.googleMapsKey.map(new GoogleMaps(_))

  override def run(): Unit = {
    println("Start LocationSetterThread")
    import models.DefaultAliases.{sg, ls}
    mapsOpt.foreach { maps =>
      Stream.from(0).map { idx =>
        val xs = LineStation.joins(LineStation.stationRef, LineStation.lineRef)
          .findAllWithLimitOffset(limit = FindCount, offset = idx * FindCount, orderings = Seq(ls.id.desc))
        xs.foreach { x =>
          val geo = StationGeo.findBy(sqls.eq(sg.stationId, x.stationId))
          if (geo.isEmpty) {
            for {
              station <- x.station
              line <- x.line
            } {
              val geo = geoQuery(maps)(s"${line.name} ${normStationName(station.name)}駅")
                .orElse(geoQuery(maps)(s"${normStationName(station.name)}駅"))
              Thread.sleep(1000L)
              geo.foreach(saveGeo(x.stationId, _)(AutoSession))
            }
          }
        }
        Thread.sleep(100)
        xs.nonEmpty
      }.takeWhile(identity).foreach(_ => ())
    }
  }
}

class LocationSetterModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) =
    bind(classOf[LocationSetter]).toSelf.eagerly() :: Nil
}
