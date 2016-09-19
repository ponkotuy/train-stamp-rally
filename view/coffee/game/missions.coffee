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
          @missions.forEach (mission) =>
            game = _.find games, (g) -> g.missionId == mission.id
            Vue.set(mission, 'game', game)
      start: (mission) ->
        API.post "/api/game/#{mission.id}", {}, ->
          @continue(mission)
      continue: (mission) ->
        location.href = "/game/game.html?mission=#{mission.id}"
    ready: ->
      @getMissions()
