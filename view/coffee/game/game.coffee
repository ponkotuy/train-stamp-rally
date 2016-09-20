$(document).ready ->
  new Vue
    el: '#game'
    data:
      missionId: 0
      game: {}
      diagrams: []
    methods:
      setMission: ->
        @missionId = fromURLParameter(location.search.slice(1)).mission
        if !@missionId
          location.href = '/game/index.html'
      getGame: ->
        API.getJSON "/api/game/#{@missionId}", (json) =>
          @game = json
          @getDiagrams()
      getDiagrams: ->
        API.getJSON "/api/diagrams?station=#{@game.station.id}&time=#{@timeFormat(@game.time)}", (json) =>
          @diagrams = json
      dateFormat: (date) ->
        "#{date.day}日目 #{@twoDigit(date.hour)}:#{@twoDigit(date.minutes)}"
      timeFormat: (time) ->
        @twoDigit(time.hour) + @twoDigit(time.minutes)
      twoDigit: (int) ->
        int.toLocaleString('en-IN', {minimumIntegerDigits: 2})
      openModal: (diagram) ->
        new Vue(modalVue(diagram))
        $('#trainModal').modal('show')
    ready: ->
      @setMission()
      @getGame()
  modalVue = (diagram) ->
    el: '#trainModal'
    data:
      diagram: diagram
      stationNames: {}
    methods:
      getStation: (stationId) ->
        API.getJSON "/api/line_station/#{stationId}", (json) =>
          Vue.set(@stationNames, json.id, json.station.name)
    ready: ->
      @diagram.stops.forEach (stop) =>
        @getStation(stop.lineStationId)
