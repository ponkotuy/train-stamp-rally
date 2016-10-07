$(document).ready ->
  new Vue(mainVue())

mainVue = ->
  el: '#game'
  mixins: [formatter]
  data:
    missionId: 0
    game: {}
    trains: []
    trainModal: null
  methods:
    setMission: ->
      @missionId = parseInt(fromURLParameter(location.search.slice(1)).mission)
      if !@missionId
        location.href = '/game/index.html'
    getGame: ->
      API.getJSON "/api/game/#{@missionId}", (json) =>
        @game = json
        new Vue(missionVue(@game.id))
        @getDiagrams()
    getDiagrams: ->
      API.getJSON "/api/diagrams?station=#{@game.station.id}&time=#{@timeFormatAPI(@game.time)}", (json) =>
        @trains = json
    openModal: (train) ->
      @trainModal.setData(train, @game)
      $(modalId).modal('show')
    here: (train) ->
      _.find train.stops, (stop) => stop.station.id == @game.station.id
  ready: ->
    @setMission()
    @getGame()
    @trainModal = new Vue(modalVue())

modalId = '#trainModal'

modalVue = ->
  el: modalId
  mixins: [formatter]
  data:
    train: {}
    game: {}
    stations: []
  methods:
    board: (to) ->
      API.putJSON
        url: '/api/game/train'
        data:
          missionId: @game.missionId
          trainId: @train.id
          fromStation: @game.station.id
          toStation: to
        success: ->
          location.reload(false)
    setStations: ->
      @stations = _.dropWhile @train.stops, (stop) => stop.station.id != @game.station.id
    setData: (train, game) ->
      @train = train
      @game = game
      @setStations()

missionVue = (gameId) ->
  el: '#mission'
  data:
    progresses: []
  methods:
    getProgresses: ->
      API.getJSON "/api/game/#{gameId}/progresses", (json) =>
        @progresses = json
  ready: ->
    @getProgresses()
