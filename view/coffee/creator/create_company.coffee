$(document).ready ->
  new Vue
    el: '#createCompany'
    data:
      name: ""
    methods:
      submit: ->
        API.postJSON
          url: '/api/company'
          data: {name: @name}
          success: ->
            location.reload(false)
