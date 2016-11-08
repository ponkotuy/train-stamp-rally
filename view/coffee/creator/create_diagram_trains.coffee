@diagramTrains =
  data:
    starts: ""
    pattern:
      start: "0700"
      end: "2300"
      period: 60
    stationUrl: ""
  methods:
    pushPattern: ->
      start = parseTime(@pattern.start)
      end = parseTime(@pattern.end)
      now = _.clone(start)
      times = while (now.isBefore(end) or now.equals(end)) and (now.isAfter(start) or now.equals(start))
        result = now.fourDigit()
        now.addMinutes(parseInt(@pattern.period))
        result
      @starts += times.join(', ')
    startsData: ->
      starts = _.flatMap @starts.split(","), (raw) ->
        start = raw.trim()
        if start then [start] else []
      if starts.length < 1
        window.alert('列車が存在しません')
        return null
