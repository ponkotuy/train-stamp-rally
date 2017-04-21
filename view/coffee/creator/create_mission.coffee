$(document).ready ->
  new Vue
    el: '#createMission'
    data:
      name: ''
      stationMaster: []
      stations: [{name: ''}, {name: ''}, {name: ''}]
      startStation: ''
      csvStations: ''
      introduction: ''
      clearText: ''
    methods:
      getStations: ->
        API.getJSON '/api/stations', (json) =>
          @stationMaster = json
          @setAutoComplete($('.autoCompleteStation'))
      setAutoComplete: (elem) ->
        elem.typeahead('destroy')
        config = {name: 'stations', display: 'name', source: stationMatcher(@stationMaster), limit: 100}
        elem.typeahead(defaultTypeaheadDesign, config)
          .on 'typeahead:selected typeahead:autocomplete', (e, datum) =>
            if e.currentTarget.id == 'start'
              @startStation = datum.name
            else
              idx = parseInt(e.currentTarget.getAttribute('data-idx'))
              @stations[idx].name = datum.name
      submit: ->
        stations = _.flatMap @stations, (s) =>
          x = @findStationId(s.name)
          if x then [x] else []
        start = @findStationId(@startStation)
        API.postJSON
          url: '/api/mission'
          data: {name: @name, stations: stations, startStation: start, introduction: @introduction, clearText: @clearText}
          success: ->
            location.reload(false)
      addStation: ->
        @stations.push({name: ''})
        @setAutoComplete($('.autoCompleteStation'))
      findStationId: (name) ->
        st = _.find @stationMaster, (s) ->
          s.name == name
        st?.id
      getRandom: (size) ->
        API.getJSON '/api/mission/random', {size: size}, (json) =>
          @startStation = json.start.name
          @stations = json.stations.map (s) -> {name: s.name}
      loadCSV: ->
        elems = _.flatMap @csvStations.split(/\r\n|\r|\n/), (xs) -> xs.split(',')
        @stations = _.flatMap elems, (raw) ->
          x = raw.trim()
          if x then [{name: x}] else []
    mounted: ->
      @getStations()
    watch:
      stations: ->
        @setAutoComplete($('.autoCompleteStation'))

stationMatcher = regexMatcherBy (x) -> x.name
