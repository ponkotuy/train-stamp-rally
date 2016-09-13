$(document).ready ->
  new Vue
    el: '#diagrams'
    data:
      diagrams: []
    methods:
      getDiagrams: ->
        $.getJSON '/api/diagrams', (json) =>
          @diagrams = json
    ready: ->
      @getDiagrams()
