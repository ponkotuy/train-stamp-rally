$(document).ready ->
  new Vue
    el: '#diagrams'
    data:
      diagrams: []
    methods:
      getDiagrams: ->
        API.getJSON '/api/diagrams', (json) =>
          @diagrams = json
    ready: ->
      @getDiagrams()
