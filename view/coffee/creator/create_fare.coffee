$(document).ready ->
  new Vue
    el: '#createFare'
    mixins: [trainTypeSelector, companySelector]
    data:
      fares: [{km: 3.0, cost: 140}, {km: 6.0, cost: 190}]
      fareTable: ''
    methods:
      addFare: ->
        last = copyObject(@fares[@fares.length - 1])
        @fares.push(last)
      loadTSV: ->
        lines = @fareTable.split('\n').map (line) -> line.split('\t')
        @fares = lines.map (line) ->
          {km: parseFloat(line[0]), cost: parseInt(line[1])}
      submit: ->
        API.postJSON
          url: "/api/company/#{@company}/type/#{@trainType}/fares"
          fares = @fares.map (f) -> {km: parseFloat(f.km), cost: parseInt(f.cost)}
          data: {fares: fares}
          success: ->
            location.reload(false)
