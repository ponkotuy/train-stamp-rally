$(document).ready ->
  new Vue
    el: '#missions'
    data:
      missions: []
    methods:
      getMissions: ->
        $.getJSON '/api/missions', (json) =>
          @missions = json
    ready: ->
      @getMissions()
