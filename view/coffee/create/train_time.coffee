
class @TrainTime
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
