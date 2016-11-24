$(document).ready ->
  new Vue
    el: '#histories'
    mixins: [momentFormat]
    data:
      histories: []
    methods:
      getData: ->
        API.getJSON '/api/histories', (json) =>
          @histories = json
    ready: ->
      @getData()
