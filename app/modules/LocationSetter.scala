package modules

import com.google.inject.{AbstractModule, Inject, Singleton}
import models.{LineStation, StationGeo}
import play.api.Configuration
import utils.{Config, GoogleMaps}
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationSetter @Inject() (config: Configuration, _p: PlayInitializer, ec: ExecutionContext) {
  import LocationSetter._
  import models.DefaultAliases.sg
  val conf = new Config(config)
  val maps = new GoogleMaps(conf.googleMapsKey)
  implicit val _ec: ExecutionContext = ec

  run()

  def run(): Unit = {
    Future {
      Iterator.from(0).map { idx =>
        val xs = LineStation.joins(LineStation.stationRef, LineStation.lineRef)
          .findAllWithLimitOffset(limit = FindCount, offset = idx * FindCount)
        xs.foreach { x =>
          val geo = StationGeo.findBy(sqls.eq(sg.stationId, x.stationId))
          if (geo.isEmpty) {
            val result = for {
              station <- x.station
              line <- x.line
              geo = maps.geocoding.request(s"${line.name} ${normStationName(station.name)}駅")
              _ = Thread.sleep(1000L)
            } yield geo
            result.foreach { geo =>
              if(geo.length == 1) {
                val loc = geo.head.geometry.location
                val sg = StationGeo(x.stationId, loc.lat, loc.lng)
                sg.save()(AutoSession)
              }
            }
          }
        }
        xs.nonEmpty
      }.takeWhile(identity).foreach(_ => ())
    }
  }
}

object LocationSetter {
  val FindCount = 100
  val ReBranket = """\(.*\)""".r
  def normStationName(str: String) = ReBranket.replaceAllIn(str, "")

}

class LocationSetterModule extends AbstractModule {
  override def configure(): Unit = bind(classOf[LocationSetter]).asEagerSingleton()
}
