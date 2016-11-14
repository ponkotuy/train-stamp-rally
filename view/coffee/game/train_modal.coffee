
@trainModalId = '#trainModal'

@trainModalVue = (gameId) ->
  el: trainModalId
  mixins: [formatter, progress]
  data:
    train: {}
    game: {}
    stations: []
    isAll: false
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
      @stations = @stations.map (st, idx) =>
        isMain = st.station.rank.value <= 3 or _.includes(dests, st.station.id) or idx == @stations.length - 1
        _.extend(st, {isMain: isMain})
    setData: (train, game) ->
      @train = train
      @game = game
      @setStations()
    switchAll: ->
      @isAll = !@isAll
  ready: ->
    @gameId = gameId

@progress =
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
