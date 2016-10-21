$(document).ready ->
  new Vue
    el: '#createAccount'
    data:
      name: ""
      email: ""
      password: ""
      retype: ""
      message: undefined
    methods:
      submit: ->
        if @retype != @password
          @message.danger("パスワードが一致しません")
          return
        if @password.length < 8
          @message.danger("passwordは8文字以上が必須です")
          return
        API.postJSON
          url: '/api/account'
          data: {name: @name, email: @email, password: @password}
          success: =>
            @alert = ""
            location.href = '/auth/session.html'
    ready: ->
      @message = vueMessage('#message')
