# require calling getStations when ready
@diagramStops =
  data:
    stops: [{departure: "0"}, {}]
    stations: []
    matcher: undefined
    scrape: ""
  methods:
    setStationIfOne: ->
      @stops.forEach (stop) =>
        @matcher stop.name, (matches) ->
          if matches.length == 1
            stop.name = matches[0].name
    getStations: (done) ->
      API.getJSON '/api/line_stations', (json) =>
        @stations = json
        for s in @stations
          s.name = "#{s.line.name} #{s.station.name}"
        @matcher = stationMatcher(@stations)
        @setAutoCompleteAll()
        done()
    getScrape: ->
      ids = parseScrapeUrl(@scrape)
      API.getJSON "/api/scrape/train/#{ids[0]}/#{ids[1]}", (json) =>
        start = json.stops[0].departure
        startTime = new TrainTime(start.hour, start.minutes)
        @stops = json.stops.filter (stop) -> not $.isEmptyObject(stop.arrive) or not $.isEmptyObject(stop.departure)
        .map (stop) ->
          arrival = if $.isEmptyObject(stop.arrive)
            null
          else new TrainTime(stop.arrive.hour, stop.arrive.minutes)
          departure = if $.isEmptyObject(stop.departure)
            null
          else new TrainTime(stop.departure.hour, stop.departure.minutes)
          {name: stop.name, arrival: arrival?.diff(startTime), departure: departure?.diff(startTime)}
    setAutoComplete: (elem) ->
      elem.typeahead('destroy')
      design = {hint: true, highlight: true}
      config = {name: 'stations', display: 'name', source: @matcher}
      elem.typeahead(design, config)
    setAutoCompleteAll: ->
      @setAutoComplete($('.autoCompleteStation'))
      @setStationIfOne()
    addStop: (idx) ->
      add = $.extend(true, {}, @stops[idx])
      @stops.splice(idx + 1, 0, add)
    deleteStop: (idx) ->
      @stops.splice(idx, 1)
      if idx == 0 and @stops.length > 0 # 出発駅変更に伴う処理
        @stops[0].arrival = ""
        diff = parseInt(@stops[0].departure)
        @stops[0].departure = 0
        for stop, idx in @stops
          if idx != 0
            if stop.arrival
              stop.arrival = parseInt(stop.arrival) - diff
            if stop.departure
              stop.departure = parseInt(stop.departure) - diff
    stopsData: ->
      isAlert = false
      stops = _.flatMap @stops, (s) =>
        if !s.name then return []
        id = @getLineStationId(s.name)
        if id
          [{lineStationId: id, arrival: parseInt(s.arrival), departure: parseInt(s.departure)}]
        else
          if !isAlert then window.alert("該当の路線or駅が見つかりません: #{s.name}")
          isAlert = true
          []
      if isAlert then return null
      stops

  watch:
    stops: ->
      @setAutoCompleteAll()

stationMatcher = (xs) ->
  (q, cb) ->
    substrRegex = new RegExp(q, 'i')
    matches = _.filter xs, (x) -> substrRegex.test(x.station.name)
    cb(matches)

parseScrapeUrl = (url) ->
  url.match(/\/detail\/(\d+)\/(\d+).htm/).slice(1, 3)
