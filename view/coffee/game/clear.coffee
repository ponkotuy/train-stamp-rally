$(document).ready ->
  new Vue
    el: '#game'
    mixins: [formatter, missionParam]
    data:
      game: {station: {id: null}, distance: 0}
      mission: {}
      accountId: 0
    methods:
      getGame: ->
        API.getJSON "/api/game/#{@missionId}", (json) =>
          @game = json
          new Vue missionVue(@game.id)
      setAccount: ->
        @accountId = parseInt(@params.account)
      getMission: ->
        API.getJSON "/api/mission/#{@missionId}", (json) =>
          @mission = json
    mounted: ->
      @setMission ->
        location.href = '/game/index.html'
      @setAccount()
      @getGame()
      @getMission()

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
    mounted: ->
      @.$nextTick =>
        @setMission ->
          location.href = '/game/index.html'
        $(modalId).modal('show')
        $(modalId).modal 'closed.bs.alert', =>
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
  mounted: ->
    @getProgresses()
