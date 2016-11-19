$(document).ready ->
  modal = new Vue(stationModal)
  new Vue(randomMission)

  new Vue
    el: '#missions'
    mixins: [formatter, pagination, startMission]
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
            game = _.find games, (g) -> g.missionId == mission.mission.id
            if game
              Vue.set(mission, 'game', game)
      gameContinue: (mission) ->
        location.href = "/game/game.html?mission=#{mission.id}"
      start: (mission) ->
        API.post "/api/game/#{mission.id}", {}, =>
          @gameContinue(mission)
      filter: (name) ->
        @rank = name
        @getData()
      openModal: (mission) ->
        modal.setMission(mission)
        $(stationModalId).modal('show')

emptyUndef = (str) ->
  if str then str else undefined
