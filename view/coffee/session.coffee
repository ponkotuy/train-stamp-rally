$(document).ready ->
  new Vue
    el: '#session'
    data:
      email: ""
      password: ""
      message: undefined
    methods:
      login: ->
        API.postJSON
          url: '/api/session'
          data: {email: @email, password: @password}
          success: ->
            location.href = fromURLParameter(location.search.slice(1)).redirect ? '/game/index.html'
          error: (e) =>
            console.log(e)
            @message.danger(e.responseText)
      setMessage: ->
        @message = vueMessage('#message')
    ready: ->
      @setMessage()
