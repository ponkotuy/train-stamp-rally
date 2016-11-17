
@stationModalId = '#stationModal'

@stationModal =
  el: stationModalId
  mixins: [formatter]
  data:
    mission: {}
    times: []
    moneys: []
    distances: []
  methods:
    setMission: (mission) ->
      @mission = mission
      @getRankings()
    getRankings: ->
      @getTimes()
      @getMoneys()
      @getDistances()
    getTimes: ->
      API.getJSON "/api/game/#{@mission.id}/ranking/time", (json) =>
        @times = json
    getMoneys: ->
      API.getJSON "/api/game/#{@mission.id}/ranking/money", (json) =>
        @moneys = json
    getDistances: ->
      API.getJSON "/api/game/#{@mission.id}/ranking/distance", (json) =>
        @distances = json
  watch: ->
    'mission.id': ->
      @getTimes()
      @getMoneys()
      @getDistances()
