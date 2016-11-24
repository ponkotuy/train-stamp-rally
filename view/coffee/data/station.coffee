$(document).ready ->
  new Vue
    el: '#main'
    mixins: [trainTypeColorClass]
    data:
      station: {}
      diagrams: []
      lines: []
      fromLines: []
    methods:
      getStation: ->
        id = fromURLParameter(location.search.slice(1))?.id
        if id
          API.getJSON "/api/station/#{id}", (json) =>
            @station = json
          API.getJSON "/api/station/#{id}/lines", (json) =>
            @lines = json
          API.getJSON "/api/diagrams", {station: id}, (json) =>
            trains = _.flatMap json, (train) =>
              train.stops = _.chain(train.stops)
                .reverse()
                .uniqBy('station.id')
                .filter (stop) -> stop.arrival? or stop.departure?
                .reverse()
                .value()
              if @here(train) then [train] else []
            @saveFromLines(trains)
      saveFromLines: (trains) ->
        fromLines = _.groupBy trains, (train) => @here(train).line.id
        for lineId, xs of fromLines
          ordered = _.chain(xs)
            .orderBy (x) -> x.subType
            .orderBy (x) -> 0 < (x.stops[0].lineStation.km - x.stops[1].lineStation.km)
            .orderBy (x) -> -x.trainType.value
            .value()
          line = _.find @lines, (l) -> l.lineId == parseInt(lineId)
          Vue.set(line, 'trains', ordered)
      here: (train) ->
        _.find train.stops, (stop) => stop.station.id == @station.id
      isLast: (idx, xs) ->
        idx == xs.length - 1
      jumpStation: (stationId) ->
        location.search = "id=#{stationId}"
        @getStation()
      stationSize: (station) ->
        'text-success': station.id == @station.id
        'large': station.id == @station.id
        'text-info': station.rank.value <= 3 && station.id != @station.id
        'text-bold': station.rank.value <= 4
    ready: ->
      @getStation()
