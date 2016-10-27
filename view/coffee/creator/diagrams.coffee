$(document).ready ->
  new Vue
    el: '#diagrams'
    data:
      diagrams: []
      pagination:
        current: 0
        size: 10
        last: 1
    methods:
      getDiagrams: ->
        API.getJSON '/api/diagrams', {page: @pagination.current + 1, count: @pagination.size}, (json) =>
          @diagrams = json.data
          @pagination.total = json.pagination.total
          @pagination.last = Math.min(json.pagination.last, 10)
      edit: (id) ->
        location.href = "?edit=#{id}"
      delete: (id) ->
        API.delete "/api/diagram/#{id}", {}, ->
          location.reload(false)
      next: (page) ->
        @pagination.current = page ? @pagination.current + 1
        @getDiagrams()
    ready: ->
      @getDiagrams()
