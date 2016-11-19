
@startMission =
  methods:
    gameContinue: (mission) ->
      console.log(mission.mission)
      location.href = "/game/game.html?mission=#{mission.mission.id}"
    start: (mission) ->
      API.post "/api/game/#{mission.mission.id}", {}, =>
        @gameContinue(mission)
