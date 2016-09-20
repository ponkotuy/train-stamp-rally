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
    ready: ->
      @setMission()
      @getGame()
