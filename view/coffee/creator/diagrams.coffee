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
      parsePageHash: ->
        page = fromURLParameter(location.hash.slice(1))?.page ? 1
        @pagination.current = page - 1
    ready: ->
      @parsePageHash()
      @getDiagrams()
    watch:
      'pagination.current': (current)->
        @getDiagrams()
        location.hash = "#page=#{parseInt(current) + 1}"
