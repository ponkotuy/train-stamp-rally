$(document).ready ->
  creator = creatorVue()
  manager = new Vue
    el: '#manager'
    data:
      name: ''
      rank: 5
      stationId: null
    methods:
      update: ->
        if @stationId?
          API.putJSON
            url: "/api/station/#{@stationId}"
            data:
              name: @name
              rankValue: parseInt(@rank)
            success: ->
              location.reload(false)
      setStation: (id) ->
        @stationId = id
        API.getJSON "/api/station/#{id}", (json) =>
          @name = json.name
          @rank = json.rank.value
  new Vue
    el: '#search'
    data:
      stations: []
      name: ''
    methods:
      search: ->
        API.getJSON '/api/stations', {q: @name}, (json) =>
          @stations = json
          @stations.forEach (station) ->
            API.getJSON "/api/station/#{station.id}/lines", (json) ->
              Vue.set(station, 'lines', json)
      update: (id) ->
        manager.setStation(id)
      createStation: (station, line) ->
        creator.lineStationLoad(station, line)
