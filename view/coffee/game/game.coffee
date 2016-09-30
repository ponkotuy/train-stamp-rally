$(document).ready ->
  new Vue
    el: '#game'
    mixins: [formatter]
    data:
      missionId: 0
      game: {}
      diagrams: []
    methods:
      setMission: ->
        @missionId = fromURLParameter(location.search.slice(1)).mission
        if !@missionId
          location.href = '/game/index.html'
      getGame: ->
        API.getJSON "/api/game/#{@missionId}", (json) =>
          @game = json
          new Vue(missionVue(@game.id))
          @getDiagrams()
      getDiagrams: ->
        API.getJSON "/api/diagrams?station=#{@game.station.id}&time=#{@timeFormatAPI(@game.time)}", (json) =>
          @diagrams = json
      openModal: (diagram) ->
        new Vue(modalVue(diagram.train.id, @game.station.id, @missionId))
        $('#trainModal').modal('show')
    ready: ->
      @setMission()
      @getGame()
  modalVue = (trainId, stationId, missionId) ->
    el: '#trainModal'
    mixins: [formatter]
    data:
      train: {}
    methods:
      getTrain: (id) ->
        API.getJSON "/api/train/#{id}", (json) =>
          stops = _.dropWhile json.stops, (stop) ->
            stop.station.id != stationId
          @train = json
          @train.stops = stops
      board: (to) ->
        API.putJSON
          url: '/api/game/train'
          data:
            missionId: missionId
            trainId: trainId
            fromStation: stationId
            toStation: to
            success: ->
              location.reload(false)
    ready: ->
      @getTrain(trainId)

  missionVue = (gameId) ->
    el: '#mission'
    data:
      progresses: []
    methods:
      getProgresses: ->
        API.getJSON "/api/game/#{gameId}/progresses", (json) =>
          @progresses = json
          console.log(@progresses)
    ready: ->
      @getProgresses()
