$(document).ready ->
  new Vue
    el: '#missions'
    mixins: [formatter]
    data:
      missions: []
      games: []
      rank: undefined
    methods:
      getMissions: ->
        API.getJSON '/api/missions', {rank: @rank, score: true}, (json) =>
          @missions = json
          @getGames()
      getGames: ->
        API.getJSON '/api/games', (games) =>
          @missions.forEach (mission) ->
            game = _.find games, (g) -> g.missionId == mission.id
            if game
              Vue.set(mission, 'game', game)
      gameContinue: (mission) ->
        location.href = "/game/game.html?mission=#{mission.mission.id}"
      start: (mission) ->
        API.post "/api/game/#{mission.id}", {}, =>
          @gameContinue(mission)
      filter: (name) ->
        @rank = name
        @getMissions()
    ready: ->
      @getMissions()

  new Vue
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
