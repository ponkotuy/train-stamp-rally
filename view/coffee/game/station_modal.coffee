
@stationModal = (el) -> new Vue
  el: el
  mixins: [formatter, startMission]
  data:
    mission: {id: null, mission: {id: null}}
    game: {}
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
      API.getJSON "/api/game/#{@mission.mission.id}/ranking/time", (json) =>
        @times = json.slice(0, 5)
    getMoneys: ->
      API.getJSON "/api/game/#{@mission.mission.id}/ranking/money", (json) =>
        @moneys = json.slice(0, 5)
    getDistances: ->
      API.getJSON "/api/game/#{@mission.mission.id}/ranking/distance", (json) =>
        @distances = json.slice(0, 5)
  watch: ->
    'mission.id': ->
      @getTimes()
      @getMoneys()
      @getDistances()
