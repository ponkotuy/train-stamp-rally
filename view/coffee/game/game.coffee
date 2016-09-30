$(document).ready ->
  new Vue
    el: '#game'
    mixins: [formatter]
    data:
      missionId: 0
      game: {}
      trains: []
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
        new Vue(modalVue(train, @game))
        $('#trainModal').modal('show')
      here: (train) ->
        _.find train.stops, (stop) => stop.station.id == @game.station.id
    ready: ->
      @setMission()
      @getGame()

  modalVue = (train, game) ->
    el: '#trainModal'
    mixins: [formatter]
    data:
      train: train
    methods:
      board: (to) ->
        API.putJSON
          url: '/api/game/train'
          data:
            missionId: game.missionId
            trainId: train.id
            fromStation: game.station.id
            toStation: to
          success: ->
            location.reload(false)
      stations: ->
        _.dropWhile train.stops, (stop) -> stop.station.id != game.station.id

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
