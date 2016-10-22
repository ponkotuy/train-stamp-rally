$(document).ready ->
  new Vue
    el: '#diagrams'
    data:
      diagrams: []
    methods:
      getDiagrams: ->
        API.getJSON '/api/diagrams', (json) =>
          @diagrams = json
      edit: (id) ->
        location.href = "?edit=#{id}"
      delete: (id) ->
        API.delete "/api/diagram/#{id}", {}, ->
          location.reload(false)
    ready: ->
      @getDiagrams()
