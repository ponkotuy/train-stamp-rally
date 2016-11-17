@randomMission =
  el: '#random'
  data:
    mission: undefined
  methods:
    getRandom: (size) ->
      API.getJSON '/api/mission/random', {size: size}, (json) =>
        @mission = json
    start: ->
      API.postJSON
        url: '/api/mission'
        data:
          name: @mission.name
          stations: @mission.stations.map (s) -> s.id
          startStation: @mission.start.id
        success: (id) ->
          API.post "/api/game/#{id}", {}, ->
            location.href = "/game/game.html?mission=#{id}"
