$(document).ready ->
  new Vue
    el: '#lines'
    data:
      lines: []
    methods:
      getLines: ->
        $.getJSON '/api/lines', (json) =>
          @lines = json
    ready: ->
      @getLines()
