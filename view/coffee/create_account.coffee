$(document).ready ->
  new Vue
    el: '#createAccount'
    data:
      name: ""
      email: ""
      password: ""
      retype: ""
      alert: undefined
    methods:
      submit: ->
        if @retype != @password
          @alert = "パスワードが一致しません"
          return
        if @password.length < 8
          @alert = "passwordは8文字以上が必須です"
          return
        API.postJSON
          url: '/api/account'
          data: {name: @name, email: @email, password: @password}
          success: =>
            @alert = ""
            location.href = '/auth/session.html'
