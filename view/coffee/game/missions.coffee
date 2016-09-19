$(document).ready ->
  new Vue
    el: '#missions'
    data:
      missions: []
    methods:
      getMissions: ->
        API.getJSON '/api/missions', (json) =>
          @missions = json
      start: (mission) ->
        API.post "/api/game/#{mission.id}", {}, ->
          location.reload(false)
    ready: ->
      @getMissions()
