@creatorVue = ->
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
              rankValue: parseInt(@rank)
            success: ->
              location.reload(false)
      lineAutoComplete: ->
        config = {name: 'lines', display: 'name', source: lineMatcher(@lines)}
        $('#lineName').typeahead(defaultTypeaheadDesign, config)
      stationAutoComplete: ->
        $('#stationName').typeahead('destroy')
        config = {name: 'stations', display: 'name', source: stationMatcher(@lineStations)}
        $('#stationName').typeahead(defaultTypeaheadDesign, config)
      lineStationLoad: (station, line) ->
        @rank = station.rank.value
        @lineName = line.line.name
        @stationName = station.name
        @getStations(line.lineId)
    mounted: ->
      @getLines()

lineMatcher = regexMatcherBy (x) -> x.name
stationMatcher = regexMatcherBy (x) -> x.station.name
