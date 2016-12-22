$(document).ready ->
  params = fromURLParameter(location.search.slice(1))
  id = params.id
  API.getJSON "/api/account"
