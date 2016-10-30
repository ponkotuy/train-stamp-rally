$(document).ready ->
  new Vue
    el: '#lines'
    data:
      lines: []
      lineModal: null
    methods:
      getLines: ->
        $.getJSON '/api/lines', (json) =>
          @lines = json
      openModal: (line) ->
        @lineModal.setLine(line)
        $(modalId).modal('show')
    ready: ->
      @getLines()
      @lineModal = new Vue(modalVue)

modalId = '#lineModal'

modalVue =
  el: modalId
  data:
    line: {}
    stations: []
  methods:
    getStations: ->
      API.getJSON "/api/line/#{@line.id}/stations", (json) =>
        @stations = json
    setLine: (line) ->
      @line = line
      @getStations()
