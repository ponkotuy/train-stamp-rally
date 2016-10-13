$(document).ready ->
  new Vue
    el: '#game'
    mixins: [formatter, missionParam]
    data:
      game: {}
    methods:
      getGame: ->
        API.getJSON "/api/game/#{@missionId}", (json) =>
          @game = json
    ready: ->
      @setMission ->
        location.href = '/game/index.html'
      @getGame()

  new Vue
    el: '#mission'
    data:
      progresses: []
    methods:
      getProgresses: ->
      API.getJSON "/api/game/#{gameId}/progresses", (json) =>
        @progresses = json
    ready: ->
      @getProgresses()

  new Vue
    el: '#ranking'
    data:
      times: []
