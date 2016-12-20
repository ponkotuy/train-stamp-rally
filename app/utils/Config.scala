package utils

import play.api.Configuration

class Config(orig: Configuration) {
  lazy val googleMapsKey: Option[String] = orig.getString("google.maps.key")
}
