$(document).ready ->
  new Vue
    el: '#session'
    data:
      email: ""
      password: ""
    methods:
      login: ->
        API.postJSON
          url: '/api/session'
          data: {email: @email, password: @password}
          success:
            location.href = fromURLParameter(location.search.slice(1)).redirect ? '/game/index.html'
