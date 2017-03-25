
class @MyMap
  constructor: (@mapId) ->
    @markers = []
    @map = L.map(@mapId)
    tiles = L.tileLayer(
      'http://{s}.maps.ponkotuy.com/maps/{z}/{x}/{y}.png',
      {attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'}
    )
    @map.addLayer(tiles)

  addMarkers: (ms) ->
    for m in ms
      marker = L.marker([m.latitude, m.longitude])
      add = marker.addTo(@map)
      if m.popup?
        add.bindPopup(m.popup)
      @markers.push(marker)

  setView: (pos, zoom = 16) ->
    @map.setView([pos.latitude, pos.longitude], zoom)

  clearMarkers: ->
    for m in @markers
      @map.removeLayer(m)
    @markers = []

@initZoom = (center, points) ->
  diffs = points.map (p) -> diff(center, p)
  meter = _.max(diffs)
  console.log(meter)
  adjustZoomLevel(meter)

diff = (x, y) ->
  lat = x.latitude - y.latitude
  lng = x.longitude - y.longitude
  lat_meter = lat * 111263.283
  lng_meter = lng * 91159.1611 # Tokyo(lat = 35.0)
  Math.sqrt(lat_meter * lat_meter + lng_meter * lng_meter)

Scales = [
  500000000, 250000000, 150000000, 70000000, 35000000, 15000000, 10000000, 4000000, 2000000, 1000000,
  500000, 250000, 150000, 70000, 35000, 15000, 8000, 4000, 2000, 1000
]

adjustZoomLevel = (meter) ->
  for rate, zoom in Scales
    if rate / 25 < meter
      return zoom
  return 19
