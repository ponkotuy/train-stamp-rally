$(document).ready ->
  new Vue
    el: '#createDiagram'
    data:
      types: []
      type: 1
      name: ""
      subtype: ""
      stops: [{trainTime: 0}, {trainTime: 1}]
    methods:
      getTypes: ->
        $.getJSON '/api/train_types', (json) =>
          @types = json
    ready: ->
      @getTypes()
