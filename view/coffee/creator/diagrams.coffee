$(document).ready ->
  new Vue
    el: '#diagrams'
    data:
      diagrams: []
      pagination:
        current: 1
        size: 10
        last: 1
    methods:
      getDiagrams: ->
        API.getJSON '/api/diagrams', {page: @pagination.current, count: @pagination.size}, (json) =>
          @diagrams = json.data
          @pagination.total = json.pagination.total
          @pagination.last = json.pagination.last
      edit: (id) ->
        location.href = "?edit=#{id}"
      delete: (id) ->
        API.delete "/api/diagram/#{id}", {}, ->
          location.reload(false)
    ready: ->
      @getDiagrams()
