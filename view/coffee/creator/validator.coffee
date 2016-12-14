$(document).ready ->
  new Vue
    el: '#messages'
    data:
      messages: []
    methods:
      getMessages: ->
        API.getJSON '/api/validator', (json) =>
          @messages = json
    mounted: ->
      @.$nextTick =>
        @getMessages()
