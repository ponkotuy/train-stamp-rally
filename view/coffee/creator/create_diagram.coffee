$(document).ready ->
  new Vue
    el: '#createDiagram'
    mixins: [trainTypeSelector, diagramStops, diagramTrains]
    data:
      update: null
      name: ""
      subType: ""
    methods:
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
        stops = @stopsData()
        starts = @startsData()
        if(stops? and starts?)
          {name: @name, trainType: parseInt(@trainType), subType: @subType, starts: starts, stops: stops}
        else
          null
      clear: ->
        location.href = location.pathname
      getLineStationId: (name) ->
        station = _.find @stations, (s) -> s.name == name
        station?.id
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
            @pushStarts(starts)
    mounted: ->
      @getStations =>
        @setUpdate()
    watch:
      'trainType': (newValue) ->
        if !@subType and newValue != 1
          type = @findTypeFromValue(newValue)
          if type
            @subType = type.name

trainFromMinutes = (minutes) ->
  new TrainTime(Math.floor(minutes / 60), minutes % 60)
