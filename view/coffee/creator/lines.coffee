$(document).ready ->
  new Vue
    el: '#lines'
    mixins: [pagination]
    data:
      lines: []
      lineModal: null
    methods:
      getPageData: (pagination, done) ->
        $.getJSON '/api/lines', {page: pagination.current + 1, size: 10}, (json) =>
          @lines = json.data
          done
            total: json.pagination.total
            last: json.pagination.last
      openModal: (line) ->
        @lineModal.setLine(line)
        $(modalId).modal('show')
    ready: ->
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
