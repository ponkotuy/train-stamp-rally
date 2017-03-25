$(document).ready ->
  width = $('#main').width()
  height = window.innerHeight - 100
  $('#map').width(width).height(height)
  map = new MyMap('map')
  params = fromURLParameter(location.search.slice(1))
  missionId = params.id
  API.getJSON "/api/mission/#{missionId}", (json) ->
    new Vue(mainVue(json))
    document.title = json.name
    center = json.startStation.geo
    points = json.stations.map (st) -> st.geo
    map.setView(center, initZoom(center, points))
    markers = json.stations
      .filter (x) -> x.geo?
      .map (x) -> $.extend(x.geo, {popup: x.name})
    map.addMarkers(markers)

mainVue = (mission) ->
  el: '#title'
  data:
    mission: mission
