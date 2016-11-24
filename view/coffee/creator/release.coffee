$(document).ready ->
  new Vue
    el: '#create'
    data:
      message: ''
    methods:
      submit: ->
        API.postJSON
          url: '/api/history'
          data: {message: @message}
          success: ->
            location.reload(false)
