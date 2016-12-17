package utils

import com.google.maps.model.{ElevationResult, LatLng}
import com.google.maps.{ElevationApi, GeoApiContext, GeocodingApi}

class GoogleMaps(key: String) {
  private val context = new GeoApiContext().setApiKey(key)
  lazy val geocoding = new Geocoding(context)
  lazy val elevation = new Elevation(context)
}

class Geocoding(context: GeoApiContext) {
  def request(address: String) =
    GeocodingApi.newRequest(context).address(address).await()
}

class Elevation(context: GeoApiContext) {
  def getByPoint(latLng: LatLng): ElevationResult =
    ElevationApi.getByPoint(context, latLng).await()

  def getByPoints(latLngs: LatLng*): Array[ElevationResult] =
    ElevationApi.getByPoints(context, latLngs: _*).await()
}
