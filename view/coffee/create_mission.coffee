$(document).ready ->
  new Vue
    el: '#createMission'
    data:
      name: ""
      stationMaster: []
      stations: [{name: ""}]
      startStation: ""
    methods:
      getStations: ->
        API.getJSON '/api/stations', (json) =>
          @stationMaster = json
          @setAutoComplete($('.autoCompleteStation'))
      setAutoComplete: (elem) ->
        design = {hint: true, highlight: true}
        config = {name: 'stations', display: 'name', source: stationMatcher(@stationMaster)}
        elem.typeahead(design, config)
      submit: ->
        stations = _.flatMap @stations, (s) =>
          x = @findStationId(s.name)
          console.log(x, s.name)
          if x then [x] else []
        start = @findStationId(@startStation)
        API.postJSON
          url: '/api/mission'
          data: {name: @name, stations: stations, startStation: start}
          success: ->
            location.reload(false)
      addStation: ->
        @stations.push({name: ""})
        @setAutoComplete($('.autoCompleteStation:last'))
      findStationId: (name) ->
        st = _.find @stationMaster, (s) -> s.name == name
        st?.id
      getRandom: (size) ->
        API.getJSON '/api/mission/random', {size: size}, (json) =>
          @startStation = json.start.name
          @stations = json.stations.map (s) -> {name: s.name}
    ready: ->
      @getStations()
    watch:
      stations: ->
        @setAutoComplete($('.autoCompleteStation:last'))


stationMatcher = (xs) ->
  (q, cb) ->
    substrRegex = new RegExp(q, 'i')
    matches = _.filter xs, (x) -> substrRegex.test(x.name)
    cb(matches)
