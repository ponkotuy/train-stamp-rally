@diagramTrains =
  data:
    startsText: ''
    pattern:
      start: '0700'
      end: '2300'
      period: 60
    stationUrl: ''
    modal: null
  methods:
    pushPattern: ->
      start = parseTime(@pattern.start)
      end = parseTime(@pattern.end)
      now = _.clone(start)
      times = while (now.isBefore(end) or now.equals(end)) and (now.isAfter(start) or now.equals(start))
        result = now.fourDigit()
        now.addMinutes(parseInt(@pattern.period))
        result
      @pushStarts(times)
    pushStarts: (times) ->
      @startsText += times.join(', ')
    startsData: ->
      starts = _.flatMap @startsText.split(','), (raw) ->
        start = raw.trim()
        if start then [start] else []
      if starts.length < 1
        window.alert('列車が存在しません')
        return null
      starts
    scrapeStation: ->
      @modal.setUrl(@stationUrl)
      $(modalId).modal('show')
  mounted: ->
    @modal = createModal(@pushStarts)
    @modal.setUrl('')

modalId = '#startsModal'
createModal = (submitF) ->
  new Vue
    el: modalId
    data:
      url: ''
      trainAttrs: []
      candidateText: ''
      candidates: []
    methods:
      submit: ->
        submitF(@candidates)
        $(modalId).modal('hide')
      getScrape: ->
        elems = parseScrapeUrl(@url)
        if !elems? then return
        API.getJSON "/api/scrape/station/#{elems[0]}/#{elems[1]}", (json) =>
          groups = _.groupBy json, (train) -> "[#{train.typ ? '普通'}] #{train.dest}"
          @trainAttrs = for label, trains of groups
            times = trains.map (train) -> new TrainTime(train.time.hour, train.time.minutes).fourDigit()
            {label: label, trains: times, check: false}
          @trainAttrs.sort (x, y) ->
            if x.label < y.label then -1
            else if x.label > y.label then 1
            else 0
      setUrl: (url) ->
        @url = url
    watch:
      trainAttrs:
        handler: (values) ->
          attrs = _.filter values, (attr) -> attr.check
          @candidates = _.flatMap attrs, (attr) -> attr.trains
        deep: true
      candidates: (values) ->
        @candidateText = values.join(', ')
      url: (value) ->
        if value
          @getScrape()

parseScrapeUrl = (url) ->
  url.match(/\/ekijikoku\/(\d+)\/(.+).htm/)?.slice(1, 3)

parseTime = (str) ->
  num = parseInt(str)
  new TrainTime(Math.floor(num / 100), num % 100)
