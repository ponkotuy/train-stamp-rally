$(document).ready ->
  params = fromURLParameter(location.search.slice(1))
  id = params.id
  pPlayer = new Promise (resolve) ->
    API.getJSON "/api/account/#{id}", (player) ->
      API.getJSON "/api/account/#{player.id}/missions", (created) ->
        resolve([player, created])
  pClear = new Promise (resolve) ->
    API.get "/api/account/#{id}/clear_count", (count) ->
      resolve(parseInt(count))
  Promise.all([pPlayer, pClear]).then (results) ->
    playerVue(results[0][0], results[1], results[0][1])

playerVue = (player, clear, created) ->
  new Vue
    el: '#main'
    data:
      player: player
      clearCount: clear
      created: created
