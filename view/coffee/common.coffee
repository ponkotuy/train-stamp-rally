
@API =
  getJSON: (url, a, b) ->
    $.getJSON(url, a, b)
      .fail(failure)
  postJSON: (obj) ->
    @json('POST', obj)
  putJSON: (obj) ->
    @json('PUT', obj)
  json: (type, obj) ->
    $.ajax
      type: type
      url: obj.url
      data: JSON.stringify obj.data
      contentType: 'application/json'
      success: obj.success
      error: failure
  post: (url, data, success) ->
    $.post(url, data, success)
      .fail(failure)
  put: (url, data, success) ->
    obj =
      url: url
      data: data
      success: success
    @json('PUT', obj)

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

@formatter =
  methods:
    dateFormat: (date) ->
      "#{date.day}日目 #{@twoDigit(date.hour)}:#{@twoDigit(date.minutes)}"
    timeFormat: (time) ->
      "#{@twoDigit(time.hour)}:#{@twoDigit(time.minutes)}"
    timeFormatAPI: (time) ->
      @twoDigit(time.hour) + @twoDigit(time.minutes)
    twoDigit: (int) ->
      int.toLocaleString('en-IN', {minimumIntegerDigits: 2})

# Game commons
@missionParam =
  data:
    missionId: 0
  methods:
    setMission: (failed)->
      @missionId = parseInt(fromURLParameter(location.search.slice(1)).mission)
      if !@missionId
        failed()
