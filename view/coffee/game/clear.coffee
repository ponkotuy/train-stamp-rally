$(document).ready ->
  new Vue
    el: '#game'
    mixins: [formatter, missionParam, startMission]
    data:
      game: {}
      accountId: 0
    methods:
      getGame: ->
        API.getJSON "/api/game/#{@missionId}", (json) =>
          @game = json
          new Vue missionVue(@game.id)
      setAccount: ->
        @accountId = parseInt(@params.account)
    ready: ->
      @setMission ->
        location.href = '/game/index.html'
      @setAccount()
      @getGame()

  ranking = new Vue
    el: '#ranking'
    mixins: [formatter, rankingView]

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
