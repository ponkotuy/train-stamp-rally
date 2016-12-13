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
        if lineModal?
          @lineModal = modalVue()
        @lineModal.setLine(line)
        $(modalId).modal('show')

modalId = '#lineModal'

modalVue = -> new Vue
  el: modalId
  data:
    line: {company: null}
    stations: []
  methods:
    getStations: ->
      API.getJSON "/api/line/#{@line.id}/stations", (json) =>
        @stations = json
    setLine: (line) ->
      @line = line
      @getStations()
