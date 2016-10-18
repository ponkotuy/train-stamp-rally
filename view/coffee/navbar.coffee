$(document).ready ->
  new Vue
    el: '#navs'
    data:
      categories: ['game', 'creator']
      category: ''
    methods:
      parseCategory: ->
        @category = location.pathname.split('/')[1]
      camelCase: (str) ->
        str.charAt(0).toUpperCase() + str.substring(1)
    ready: ->
      @parseCategory()
