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
          new Vue missionVue(@game.id)
    ready: ->
      @setMission ->
        location.href = '/game/index.html'
      @getGame()

  ranking = new Vue
    el: '#ranking'
    mixins: [formatter, missionParam]
    data:
      times: []
      moneys: []
      distances: []
    methods:
      getTimes: ->
        API.getJSON "/api/game/#{@missionId}/ranking/time", (json) =>
          @times = json
      getMoneys: ->
        API.getJSON "/api/game/#{@missionId}/ranking/money", (json) =>
          @moneys = json
      getDistances: ->
        API.getJSON "/api/game/#{@missionId}/ranking/distance", (json) =>
          @distances = json
      reload: ->
        @getTimes()
        @getMoneys()
        @getDistances()
    ready: ->
      @setMission ->
        location.href = '/game/index.html'
      @reload()

  modalId = '#complete'
  new Vue
    el: modalId
    mixins: [missionParam]
    methods:
      clear: (rate) ->
        API.putJSON
          url: "/api/game/#{@missionId}/clear"
          data:
            rate: rate
          success: ->
            $(modalId).modal('hide')
            ranking.reload()
    ready: ->
      @setMission ->
        location.href = '/game/index.html'
      $(modalId).modal('show')
      $(modalId).modal 'closed.bs.alert', ->
        @clear(1)

missionVue = (gameId) ->
  el: '#mission'
  mixins: [formatter]
  data:
    progresses: []
  methods:
    getProgresses: ->
      API.getJSON "/api/game/#{gameId}/progresses", (json) =>
        @progresses = json
  ready: ->
    @getProgresses()
