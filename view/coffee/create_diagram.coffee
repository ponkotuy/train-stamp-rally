$(document).ready ->
  new Vue
    el: '#createDiagram'
    data:
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
      scrape: ""
    methods:
      getTypes: ->
        API.getJSON '/api/train_types', (json) =>
          @types = json
      getStations: ->
        API.getJSON '/api/line_stations', (json) =>
          @stations = json
          for s in @stations
            s.name = "#{s.line.name} #{s.station.name}"
          @setAutoCompleteAll()
      getScrape: ->
        ids = @parseScrapeUrl(@scrape)
        API.getJSON "/api/scrape/train/#{ids[0]}/#{ids[1]}", (json) =>
          start = json.stops[0].departure
          startTime = new TrainTime(start.hour, start.minutes)
          @stops = json.stops.filter (stop) -> not $.isEmptyObject(stop.arrive) or not $.isEmptyObject(stop.departure)
            .map (stop) ->
              arrival = if $.isEmptyObject(stop.arrive) then null else new TrainTime(stop.arrive.hour, stop.arrive.minutes)
              departure = if $.isEmptyObject(stop.departure) then null else new TrainTime(stop.departure.hour, stop.departure.minutes)
              {name: stop.name, arrival: arrival?.diff(startTime), departure: departure?.diff(startTime)}
      setAutoComplete: (elem) ->
        elem.typeahead('destroy')
        design = {hint: true, highlight: true}
        config = {name: 'stations', display: 'name', source: stationMatcher(@stations)}
        elem.typeahead(design, config)
      setAutoCompleteAll: ->
        @setAutoComplete($('.autoCompleteStation'))
      pushPattern: ->
        start = parseTime(@pattern.start)
        end = parseTime(@pattern.end)
        now = _.clone(start)
        times = while (now.isBefore(end) or now.equals(end)) and (now.isAfter(start) or now.equals(start))
          result = now.fourDigit()
          now.addMinutes(@pattern.period)
          result
        @starts += times.join(', ')
      addStop: (idx) ->
        add = $.extend(true, {}, @stops[idx])
        @stops.splice(idx + 1, 0, add)
      submit: ->
        stops = _.flatMap @stops, (s) =>
          id = @getLineStationId(s.name)
          if id
            [{lineStationId: id, arrival: parseInt(s.arrival), departure: parseInt(s.departure)}]
          else []
        starts = for s in @starts.split(",")
          s.trim()
        data = {name: @name, trainType: parseInt(@trainType), subType: @subType, starts: starts, stops: stops}
        API.postJSON
          url: '/api/diagram'
          data: data
          success: ->
            location.reload(false)
      getLineStationId: (name) ->
        station = _.find @stations, (s) -> s.name == name
        station?.id
      parseScrapeUrl: (url) ->
        url.match(/\/detail\/(\d+)\/(\d+).htm/).slice(1, 3)
    ready: ->
      @getTypes()
      @getStations()
    watch:
      stops: ->
        @setAutoCompleteAll()

class TrainTime
  constructor: (@hour, @minutes) ->
    @normalize()

  value: ->
    60 * @hour + @minutes

  addMinutes: (minutes) ->
    @minutes += minutes
    @normalize()

  normalize: ->
    while 60 <= @minutes
      @hour += 1
      @minutes -= 60
    while 24 <= @hour
      @hour -= 24

  fourDigit: ->
    @hour.toLocaleString("en-IN", {minimumIntegerDigits: 2}) +
      @minutes.toLocaleString("en-IN", {minimumIntegerDigits: 2})

  isBefore: (time) ->
    @fourDigit() < time.fourDigit()

  isAfter: (time) ->
    time.fourDigit() < @fourDigit()

  equals: (time) ->
    time.fourDigit() == @fourDigit()

  diff: (time) ->
    Math.abs(@value() - time.value())

parseTime = (str) ->
  num = parseInt(str)
  new TrainTime(num / 100, num % 100)

trainFromMinutes = (minutes) ->
  new TrainTime(minutes / 60, minutes % 60)

stationMatcher = (xs) ->
  (q, cb) ->
    substrRegex = new RegExp(q, 'i')
    matches = _.filter xs, (x) -> substrRegex.test(x.line.name) or substrRegex.test(x.station.name)
    cb(matches)
