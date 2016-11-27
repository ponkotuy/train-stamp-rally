$(document).ready ->
  new Vue
    el: '#create'
    data:
      message: ''
      releaseSelector: [{id: undefined, name: 'なし'}]
      releases: []
      release: undefined
    methods:
      submit: ->
        API.postJSON
          url: '/api/history'
          data: {message: @message, release: parseInt(@release)}
          success: ->
            location.reload(false)
      getReleases: ->
        API.getJSON '/api/releases', (json) =>
          @releases = json
          for r in json
            @releaseSelector.push({id: r.id, name: r.id})
    ready: ->
      @getReleases()
