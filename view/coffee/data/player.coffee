$(document).ready ->
  params = fromURLParameter(location.search.slice(1))
  id = params.id
  pPlayer = new Promise (resolve) ->
    API.getJSON "/api/account/#{id}", (player) ->
      resolve(player)
  pClear = new Promise (resolve) ->
    API.get "/api/account/#{id}/clear_count", (count) ->
      resolve(parseInt(count))
  Promise.all([pPlayer, pClear]).then (results) ->
    playerVue(results[0], results[1])

playerVue = (player, clear) ->
  new Vue
    el: '#main'
    data:
      player: player
      clearCount: clear
