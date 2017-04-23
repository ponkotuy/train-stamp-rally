$(document).ready ->
  API.getJSON '/api/account', (account) ->
    API.getJSON '/api/missions', {creator: account.id}, (json) ->
      renderMission(json)

modalEl = '#updateMissionModal'
renderMission = (missions) ->
  new Vue
    el: '#updateMission'
    data:
      missions: missions
      modal: null
    methods:
      del: (m) ->
        console.log(m)
        API.delete "/api/mission/#{m.id}", ->
          location.reload(false)
      update: (m) ->
        @modal.setMission(m)
        $(modalEl).modal('show')
    mounted: ->
      @modal = updateModal(modalEl)
