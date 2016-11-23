$(document).ready ->
  new Vue
    el: '#navs'
    data:
      categories: ['game', 'creator', 'readme']
      category: ''
      account: null
    methods:
      parseCategory: ->
        @category = location.pathname.split('/')[1]
      getAccount: ->
        $.getJSON '/api/account', (json) => @account = json
      camelCase: (str) ->
        str.charAt(0).toUpperCase() + str.substring(1)
      logout: ->
        API.delete '/api/session', {}, ->
          location.href = '/'
    ready: ->
      @parseCategory()
      @getAccount()
