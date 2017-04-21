$(document).ready ->
  API.getJSON '/api/account', (account) ->
    API.getJSON '/api/missions', {creator: account.id}, (json) ->
      renderMission(json)

renderMission = (missions) ->
  new Vue
    el: '#updateMission'
    data:
      missions: missions
    methods:
      removable: (m) ->
        true
