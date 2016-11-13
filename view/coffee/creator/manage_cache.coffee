$(document).ready ->
  new Vue
    el: '#cache'
    methods:
      clearCache: ->
        API.delete '/api/cache', {}, ->
          window.alert('Claer cacheを受け付けました')
