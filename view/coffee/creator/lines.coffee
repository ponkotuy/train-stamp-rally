$(document).ready ->
  new Vue
    el: '#lines'
    mixins: [pagination]
    data:
      lines: []
      lineModal: null
      lineName: ''
    methods:
      getPageData: (pagination, done) ->
        q = if @lineName then @lineName else undefined
        $.getJSON '/api/lines', {page: pagination.current + 1, size: 10, q: q}, (json) =>
          @lines = json.data
          done
            total: json.pagination.total
            last: Math.min(json.pagination.last, 10)
      openModal: (line) ->
        if lineModal?
          @lineModal = modalVue(line)
        $(modalId).modal('show')

modalId = '#lineModal'

modalVue = (line) -> new Vue
  el: modalId
  data:
    line: line
    stations: []
  methods:
    getStations: ->
      API.getJSON "/api/line/#{@line.id}/stations", (json) =>
        @stations = json
  mounted: ->
    @.$nextTick =>
      @getStations()
