$(document).ready ->
  new Vue
    el: '#createDiagram'
    data:
      update: null
      types: []
      trainType: 1
      name: ""
      subType: ""
      stops: [{departure: "0"}, {}]
      starts: ""
      pattern:
        start: "0700"
        end: "2300"
        period: 60
      stations: []
      matcher: undefined
      scrape: ""
    methods:
      getTypes: ->
        API.getJSON '/api/train_types', (json) =>
          @types = json
      getStations: (done) ->
        API.getJSON '/api/line_stations', (json) =>
          @stations = json
          for s in @stations
            s.name = "#{s.line.name} #{s.station.name}"
          @matcher = stationMatcher(@stations)
          @setAutoCompleteAll()
          done()
      getScrape: ->
        ids = @parseScrapeUrl(@scrape)
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
      setStationIfOne: ->
        @stops.forEach (stop) =>
          @matcher stop.name, (matches) ->
            if matches.length == 1
              stop.name = matches[0].name
      pushPattern: ->
        start = parseTime(@pattern.start)
        end = parseTime(@pattern.end)
        now = _.clone(start)
        times = while (now.isBefore(end) or now.equals(end)) and (now.isAfter(start) or now.equals(start))
          result = now.fourDigit()
          now.addMinutes(parseInt(@pattern.period))
          result
        @starts += times.join(', ')
      addStop: (idx) ->
        add = $.extend(true, {}, @stops[idx])
        @stops.splice(idx + 1, 0, add)
      deleteStop: (idx) ->
        @stops.splice(idx, 1)
      submit: ->
        data = @createData()
        if data
          API.postJSON
            url: '/api/diagram'
            data: data
            success: ->
              location.reload(false)
      updateDiagram: ->
        data = @createData()
        if @update and data
          API.putJSON
            url: "/api/diagram/#{@update}"
            data: data
            success: ->
              location.href = location.pathname
      createData: ->
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
        starts = _.flatMap @starts.split(","), (raw) ->
          start = raw.trim()
          if start then [start] else []
        console.log(starts)
        if starts.length < 1
          window.alert('列車が存在しません')
          return null
        {name: @name, trainType: parseInt(@trainType), subType: @subType, starts: starts, stops: stops}
      clear: ->
        location.href = location.pathname
      getLineStationId: (name) ->
        station = _.find @stations, (s) -> s.name == name
        station?.id
      parseScrapeUrl: (url) ->
        url.match(/\/detail\/(\d+)\/(\d+).htm/).slice(1, 3)
      setUpdate: ->
        @update = fromURLParameter(location.search.slice(1))?.edit
        if @update
          API.getJSON "/api/diagram/#{@update}", (json) =>
            @trainType = json.trainType.value
            @name = json.name
            @subType = json.subType
            @stops = json.stops.map (stop) ->
              {name: "#{stop.line.name} #{stop.station.name}", arrival: stop.arrival, departure: stop.departure}
            starts = json.trains.map (train) ->
              (new TrainTime(train.start.hour, train.start.minutes)).fourDigit()
            @starts = starts.join(', ')
    ready: ->
      @getTypes()
      @getStations =>
        @setUpdate()
    watch:
      stops: ->
        @setAutoCompleteAll()

parseTime = (str) ->
  num = parseInt(str)
  new TrainTime(Math.floor(num / 100), num % 100)

trainFromMinutes = (minutes) ->
  new TrainTime(Math.floor(minutes / 60), minutes % 60)

stationMatcher = (xs) ->
  (q, cb) ->
    substrRegex = new RegExp(q, 'i')
    matches = _.filter xs, (x) -> substrRegex.test(x.station.name)
    cb(matches)
