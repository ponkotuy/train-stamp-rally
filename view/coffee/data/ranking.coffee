$(document).ready ->
  new Vue
    el: '#main'
    mixins: [formatter, rankingView]
    data:
      mission: {}
      stations: []
    methods:
      getMission: ->
        API.getJSON "/api/mission/#{@missionId}", (json) =>
          @mission = json
      getStations: ->

    mounted: ->
      @getMission()
