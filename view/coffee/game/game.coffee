$(document).ready ->
  new Vue(mainVue())

mainVue = ->
  el: '#game'
  mixins: [formatter, missionParam]
  data:
    game: {}
    trains: []
    trainModal: null
  methods:
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
    @setMission ->
      location.href = '/game/index.html'
    @getGame()
    @trainModal = new Vue(modalVue(@game.id))

modalId = '#trainModal'

modalVue = (gameId) ->
  el: modalId
  mixins: [formatter, progress]
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
      dests = @progresses.map (p) -> p.station.id
      @stations = _.filter @stations, (st) ->
        st.station.rank.value <= 3 || _.includes(dests, st.station.id)
    setData: (train, game) ->
      @train = train
      @game = game
      @setStations()
  compiled: ->
    @gameId = gameId

missionVue = (gameId) ->
  el: '#mission'
  mixins: [progress]
  compiled: ->
    @gameId = gameId

progress =
  mixins: [missionParam]
  data:
    gameId: 0
    progresses: []
  methods:
    getProgresses: ->
      API.getJSON "/api/game/#{@gameId}/progresses", (json) =>
        @progresses = json
        if _.every(@progresses, (p) -> p.arrivalTime)
          location.href = "/game/clear.html?mission=#{@missionId}"
  ready: ->
    @setMission ->
      location.href = '/game/index.html'
    @getProgresses()
