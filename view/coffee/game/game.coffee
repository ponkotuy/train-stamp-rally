$(document).ready ->
  new Vue
    el: '#game'
    data:
      missionId: 0
      game: {}
    methods:
      setMission: ->
        @missionId = fromURLParameter(location.search.slice(1)).mission
        if !@missionId
          location.href = '/game/index.html'
      getGame: ->
        API.getJSON "/api/game/#{@missionId}", (json) =>
          @game = json
      timeFormat: (time) ->
        hour = time.hour.toLocaleString('en-IN', {minimumIntegerDigits: 2})
        minutes = time.minutes.toLocaleString('en-IN', {minimumIntegerDigits: 2})
        "#{time.day}日目 #{hour}:#{minutes}"
    ready: ->
      @setMission()
      @getGame()
