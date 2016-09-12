$(document).ready ->
  new Vue
    el: '#createDiagram'
    data:
      types: []
      type: 1
      name: ""
      subtype: ""
      stops: [{trainTime: 0}, {trainTime: 1}]
      starts: ""
      pattern:
        start: "0700"
        end: "2300"
        period: 60
      stations: []
      stationNames: []
    methods:
      getTypes: ->
        $.getJSON '/api/train_types', (json) =>
          @types = json
      getStations: ->
        $.getJSON '/api/stations', (json) =>
          @stations = json
          @setAutoComplete()
      setAutoComplete: ->
        @stationNames = for s in @stations
          "#{s.line.name} #{s.station.name}"
        $('.autoCompleteStation')
          .typeahead({hint: true, highlight: true}, {name: 'stations', source: substringMatcher(@stationNames)})
      pushPattern: ->
        start = parseTime(@pattern.start)
        end = parseTime(@pattern.end)
        now = _.clone(start)
        times = while (now.isBefore(end) or now.equals(end)) and (now.isAfter(start) or now.equals(start))
          result = now.fourDigit().toString()
          now.addMinutes(@pattern.period)
          result
        @starts += times.join(', ')
    ready: ->
      @getTypes()
      @getStations()

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
    @hour * 100 + @minutes

  isBefore: (time) ->
    @fourDigit() < time.fourDigit()

  isAfter: (time) ->
    time.fourDigit() < @fourDigit()

  equals: (time) ->
    time.fourDigit() == @fourDigit()

parseTime = (str) ->
  num = parseInt(str)
  new TrainTime(num / 100, num % 100)

substringMatcher = (xs) ->
  (q, cb) ->
    substrRegex = new RegExp(q, 'i')
    matches = _.filter xs, (x) ->substrRegex.test(x)
    cb(matches)
