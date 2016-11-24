
@startMission =
  methods:
    gameContinue: (mission) ->
      location.href = "/game/game.html?mission=#{mission.mission.id}"
    start: (mission) ->
      API.post "/api/game/#{mission.mission.id}", {}, =>
        @gameContinue(mission)
