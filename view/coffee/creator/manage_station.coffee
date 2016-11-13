$(document).ready ->
  manager = new Vue
    el: '#manager'
    data:
      name: ''
      rank: 0
      stationId: null
    methods:
      update: ->
        if @stationId?
          API.putJSON
            url: "/api/station/#{@stationId}"
            data:
              name: @name
              rankValue: @rank
            success: ->
              location.reload(false)
      setStation: (id) ->
        @stationId = id
        API.getJSON "/api/station/#{id}", (json) =>
          @name = json.name
          @rank = json.rank.value
  new Vue
    el: '#search'
    data:
      stations: []
      name: ''
    methods:
      search: ->
        API.getJSON '/api/stations', {q: @name}, (json) =>
          @stations = json
          @stations.forEach (station) ->
            API.getJSON "/api/station/#{station.id}/lines", (json) ->
              Vue.set(station, 'lines', json)
      update: (id) ->
        manager.setStation(id)
  new Vue
    el: '#creator'
    data:
      name: '' # 作る駅名
      rank: 5
      lineName: ''
      stationName: '' # 置換するline_stationの駅名
      lines: []
      lineStations: []
    methods:
      getLines: ->
        API.getJSON '/api/lines', (json) =>
          @lines = json
          @lineAutoComplete()
      getStations: (lineId) ->
        API.getJSON "/api/line/#{lineId}/stations", (json) =>
          @lineStations = json.map (x) ->
            _.extend(x, {name: x.station.name})
          @stationAutoComplete()
      updateStations: ->
        line = @findLine()
        if line?
          @getStations(line.id)
      findLine: ->
        _.find @lines, (line) =>
          line.name == @lineName
      findStation: ->
        _.find @lineStations, (ls) => ls.station.name == @stationName
      create: ->
        if !@name
          window.alert('駅名を設定してください')
          return
        station = @findStation()
        if station?
          API.putJSON
            url: "/api/line_station/#{station.id}"
            data:
              name: @name
              rankValue: @rank
            success: ->
              location.reload(false)
      lineAutoComplete: ->
        config = {name: 'lines', display: 'name', source: lineMatcher(@lines)}
        $('#lineName').typeahead(defaultTypeaheadDesign, config)
      stationAutoComplete: ->
        $('#stationName').typeahead('destroy')
        config = {name: 'stations', display: 'name', source: stationMatcher(@lineStations)}
        $('#stationName').typeahead(defaultTypeaheadDesign, config)
    ready: ->
      @getLines()

lineMatcher = regexMatcherBy (x) -> x.name
stationMatcher = regexMatcherBy (x) -> x.station.name
