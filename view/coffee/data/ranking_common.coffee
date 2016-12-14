
@rankingView =
  mixins: [missionParam]
  data:
    times: []
    moneys: []
    distances: []
    accountId: 0
  methods:
    getTimes: ->
      API.getJSON "/api/game/#{@missionId}/ranking/time", (json) =>
        @times = json
    getMoneys: ->
      API.getJSON "/api/game/#{@missionId}/ranking/money", (json) =>
        @moneys = json
    getDistances: ->
      API.getJSON "/api/game/#{@missionId}/ranking/distance", (json) =>
        @distances = json
    reload: ->
      @getTimes()
      @getMoneys()
      @getDistances()
    setAccount: ->
      @accountId = parseInt(@params.account)
  mounted: ->
    @setMission ->
      location.href = '/game/index.html'
    @setAccount()
    @reload()
