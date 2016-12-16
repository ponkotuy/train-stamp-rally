$(document).ready ->
  createNav(location.pathname.split('/')[1])
  $.getJSON '/api/account', (account) ->
    createLogin(account)
  .fail ->
    createLogin(undefined)

createLogin = (account) ->
  new Vue
    el: '#login'
    data:
      logined: account?
      account: account
    methods:
      logout: ->
        API.delete '/api/session', {}, ->
          location.href = '/'

createNav = (category) ->
  new Vue
    el: '#navs'
    data:
      categories: ['game', 'creator', 'readme']
      category: category
    methods:
      camelCase: (str) ->
        str.charAt(0).toUpperCase() + str.substring(1)
