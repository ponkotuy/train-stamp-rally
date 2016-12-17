package utils

import play.api.Configuration

class Config(orig: Configuration) {
  lazy val googleMapsKey = orig.getString("google.maps.key").get
}
