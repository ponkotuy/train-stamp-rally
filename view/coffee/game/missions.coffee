$(document).ready ->
  new Vue
    el: '#missions'
    data:
      missions: []
      games: []
    methods:
      getMissions: ->
        API.getJSON '/api/missions', (json) =>
          @missions = json
          @getGames()
      getGames: ->
        API.getJSON '/api/games', (games) =>
          @missions.forEach (mission) ->
            game = _.find games, (g) -> g.missionId == mission.id
            Vue.set(mission, 'game', game)
      gameContinue: (mission) ->
        location.href = "/game/game.html?mission=#{mission.id}"
      start: (mission) ->
        API.post "/api/game/#{mission.id}", {}, ->
          @gameContinue(mission)
    ready: ->
      @getMissions()
