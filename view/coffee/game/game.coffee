$(document).ready ->
  new Vue(mainVue())

mainVue = ->
  el: '#game'
  mixins: [formatter, missionParam]
  data:
    game: {}
    lines: []
    fromLines: []
    trainModal: null
  methods:
    getGame: ->
      API.getJSON "/api/game/#{@missionId}", (json) =>
        @game = json
        new Vue(missionVue(@game.id))
        @getDiagrams()
        @trainModal = new Vue(trainModalVue(@game.id))
    getDiagrams: ->
      API.getJSON "/api/diagrams?station=#{@game.station.id}&time=#{@timeFormatAPI(@game.time)}", (json) =>
        trains = json.map (train) =>
          train.stops = _.chain(train.stops)
            .reverse()
            .uniqBy('station.id')
            .filter (stop) -> stop.arrival or stop.departure
            .reverse()
            .value()
          train
        @saveLines(trains)
        @saveFromLines(trains)
    saveLines: (trains) ->
      for train in trains
        for stop in train.stops
          @lines[stop.line.id] = stop.line
    saveFromLines: (trains) ->
      fromLines = _.groupBy trains, (train) => @here(train).line.id
      @fromLines = for lineId, xs of fromLines
        ordered = _.chain(xs)
          .orderBy (x) => @timeFormat(@here(x).departure)
          .orderBy (x) -> x.subType
          .orderBy (x) -> 0 < (x.stops[0].lineStation.km - x.stops[1].lineStation.km)
          .orderBy (x) -> -x.trainType.value
          .value()
        _.extend(@lines[lineId], {trains: ordered})
    openModal: (train) ->
      @trainModal.setData(train, @game)
      $(trainModalId).modal('show')
    here: (train) ->
      _.find train.stops, (stop) => stop.station.id == @game.station.id
  ready: ->
    @setMission ->
      location.href = '/game/index.html'
    @getGame()

missionVue = (gameId) ->
  el: '#mission'
  mixins: [progress]
  compiled: ->
    @gameId = gameId
