$(document).ready ->
  new Vue
    el: '#diagrams'
    mixins: [pagination]
    data:
      diagrams: []
      lineName: ""
    methods:
      getPageData: (page, done) ->
        params =
          page: page.current + 1
          count: 10
          lineName: if @lineName then @lineName else undefined
        API.getJSON '/api/diagrams', params, (json) =>
          @diagrams = json.data
          done
            total: json.pagination.total
            last: Math.min(json.pagination.last, 10)
      edit: (id) ->
        location.href = "?edit=#{id}"
      delete: (id) ->
        API.delete "/api/diagram/#{id}", {}, ->
          location.reload(false)
