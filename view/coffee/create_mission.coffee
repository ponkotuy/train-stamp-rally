$(document).ready ->
  new Vue
    el: '#createMission'
    data:
      name: ""
      stationMaster: []
      stations: [{name: ""}]
    methods:
      getStations: ->
        $.getJSON '/api/stations', (json) =>
          @stationMaster = json
          console.log(json, )
          @setAutoComplete($('.autoCompleteStation'))
      setAutoComplete: (elem) ->
        design = {hint: true, highlight: true}
        config = {name: 'stations', display: 'name', source: stationMatcher(@stationMaster)}
        elem.typeahead(design, config)
      submit: ->
        stations = _.flatMap @stations, (s) =>
          x = _.find @stationMaster, (sm) -> sm.name == s.name
          if x then [x.id] else []
        postJSON
          url: '/api/mission'
          data: {name: @name, stations: stations}
          success: ->
            # location.reload(false)
      addStation: ->
        @stations.push({name: ""})
        @setAutoComplete($('.autoCompleteStation:last'))
    ready: ->
      @getStations()

stationMatcher = (xs) ->
  (q, cb) ->
    substrRegex = new RegExp(q, 'i')
    matches = _.filter xs, (x) -> substrRegex.test(x.name)
    cb(matches)
