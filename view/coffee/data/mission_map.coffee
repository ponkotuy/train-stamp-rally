$(document).ready ->
  width = $('#main').width()
  height = window.innerHeight - 100
  $('#map').width(width).height(height)
  map = new MyMap('map')
  params = fromURLParameter(location.search.slice(1))
  missionId = params.id
  map.setView({latitude: 37.786941, longitude: 138.4089693}, 7)
  API.getJSON "/api/mission/#{missionId}", (json) ->
    new Vue(mainVue(json))
    document.title = json.name
    markers = json.stations
      .filter (x) -> x.geo?
      .map (x) -> $.extend(x.geo, {popup: x.name})
    map.addMarkers(markers)

class MyMap
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

mainVue = (mission) ->
  el: '#title'
  data:
    mission: mission
