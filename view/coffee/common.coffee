
@postJSON = (obj) ->
  $.ajax
    type: 'POST'
    url: obj.url
    data: JSON.stringify obj.data
    contentType: 'application/json'
    success: obj.success
