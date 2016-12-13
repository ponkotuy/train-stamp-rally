$(document).ready ->
  new Vue
    el: '#createMission'
    data:
      name: ""
      stationMaster: []
      stations: [{name: ""}, {name: ""}, {name: ""}]
      startStation: ""
    methods:
      getStations: ->
        API.getJSON '/api/stations', (json) =>
          @stationMaster = json
          @setAutoComplete($('.autoCompleteStation'))
      setAutoComplete: (elem) ->
        elem.typeahead('destroy')
        config = {name: 'stations', display: 'name', source: stationMatcher(@stationMaster), limit: 100}
        elem.typeahead(defaultTypeaheadDesign, config)
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
        @setAutoComplete($('.autoCompleteStation'))
      findStationId: (name) ->
        st = _.find @stationMaster, (s) -> s.name == name
        st?.id
      getRandom: (size) ->
        API.getJSON '/api/mission/random', {size: size}, (json) =>
          @startStation = json.start.name
          @stations = json.stations.map (s) -> {name: s.name}
    mounted: ->
      @getStations()
    watch:
      stations: ->
        @setAutoComplete($('.autoCompleteStation'))

stationMatcher = regexMatcherBy (x) -> x.name
