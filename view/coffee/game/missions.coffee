$(document).ready ->
  modal = new Vue
    el: modalId
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

  new Vue
    el: '#missions'
    mixins: [formatter, pagination]
    data:
      missions: []
      games: []
      rank: undefined
      stationName: ''
      missionName: ''
    methods:
      getPageData: (page, done) ->
        q =
          rank: @rank
          score: true
          page: page.current + 1
          size: 10
          station_name: emptyUndef(@stationName)
          name: emptyUndef(@missionName)
        API.getJSON '/api/missions', q, (json) =>
          @missions = json.data
          @getGames()
          done(json.pagination)
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
        @getData()
      openModal: (mission) ->
        modal.setMission(mission)
        $(modalId).modal('show')

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

modalId = '#stationModal'
emptyUndef = (str) ->
  if str then str else undefined
