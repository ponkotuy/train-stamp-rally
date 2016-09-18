$(document).ready ->
  $('form').each ->
    action = $(@).attr('action')
    method = $(@).attr('method')
    if action and method
      $(@).find('button.submit').click =>
        data = {}
        $(@).find('[name]').each ->
          name = $(@).attr('name')
          value = $(@).val()
          data[name] = value
        $.ajax
          type: method
          url: action
          contentType: 'application/json'
          data: JSON.stringify(data)
          success: ->
            location.href = '/game/index.html'
