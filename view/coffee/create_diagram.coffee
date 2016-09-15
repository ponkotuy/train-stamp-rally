$(document).ready ->
  new Vue
    el: '#createDiagram'
    data:
      types: []
      trainType: 1
      name: ""
      subType: ""
      stops: [{departure: "0"}, {arrival: "1"}]
      starts: ""
      pattern:
        start: "0700"
        end: "2300"
        period: 60
      stations: []
    methods:
      getTypes: ->
        $.getJSON '/api/train_types', (json) =>
          @types = json
      getStations: ->
        $.getJSON '/api/lineStations', (json) =>
          @stations = json
          for s in @stations
            s.name = "#{s.line.name} #{s.station.name}"
          @setAutoComplete($('.autoCompleteStation'))
      setAutoComplete: (elem) ->
        design = {hint: true, highlight: true}
        config = {name: 'stations', display: 'name', source: stationMatcher(@stations)}
        elem.typeahead(design, config)
      pushPattern: ->
        start = parseTime(@pattern.start)
        end = parseTime(@pattern.end)
        now = _.clone(start)
        console.log(now.fourDigit())
        times = while (now.isBefore(end) or now.equals(end)) and (now.isAfter(start) or now.equals(start))
          result = now.fourDigit()
          now.addMinutes(@pattern.period)
          result
        @starts += times.join(', ')
      addStop: ->
        @stops.push({minutes: 0})
      submit: ->
        stops = _.flatMap @stops, (s) =>
          id = @getLineStationId(s.name)
          if id
            [{lineStationId: id, arrival: parseInt(s.arrival), departure: parseInt(s.departure)}]
          else []
        starts = for s in @starts.split(",")
          s.trim()
        data = {name: @name, trainType: parseInt(@trainType), subType: @subType, starts: starts, stops: stops}
        postJSON
          url: '/api/diagram'
          data: data
          success: ->
            location.reload(false)
      getLineStationId: (name) ->
        station = _.find @stations, (s) -> s.name == name
        station?.id
    ready: ->
      @getTypes()
      @getStations()
    watch:
      stops: ->
        @setAutoComplete($('.autoCompleteStation:last'))


class TrainTime
  constructor: (@hour, @minutes) ->
    @normalize()
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

parseTime = (str) ->
  num = parseInt(str)
  new TrainTime(num / 100, num % 100)

stationMatcher = (xs) ->
  (q, cb) ->
    substrRegex = new RegExp(q, 'i')
    matches = _.filter xs, (x) -> substrRegex.test(x.line.name) or substrRegex.test(x.station.name)
    cb(matches)
