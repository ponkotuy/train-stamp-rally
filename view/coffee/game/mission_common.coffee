
@startMission =
  methods:
    gameContinue: (missionId) ->
      location.href = "/game/game.html?mission=#{missionId}"
    start: (missionId) ->
      API.post "/api/game/#{missionId}", {}, =>
        @gameContinue(missionId)
