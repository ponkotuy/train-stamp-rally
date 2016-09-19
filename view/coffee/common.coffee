
@API =
  getJSON: (url, f) ->
    $.getJSON(url, f)
      .fail(failure)
  postJSON: (obj) ->
    $.ajax
      type: 'POST'
      url: obj.url
      data: JSON.stringify obj.data
      contentType: 'application/json'
      success: obj.success
      error: failure
  post: (url, data, success) ->
    $.post(url, data, success)
      .fail(failure)

failure = (jqXHR) ->
  redirect = location.pathname
  if jqXHR.status == 403
    location.href = "/auth/session.html?redirect=#{redirect}"

@fromURLParameter = (str) ->
  obj = {}
  for kv in str.split('&')
    ary = kv.split('=')
    key = ary.shift()
    obj[key] = ary.join('=')
  obj
