$(document).ready ->
  new Vue
    el: '#companies'
    data:
      companies: []
    methods:
      getCompanies: ->
        API.getJSON '/api/companies', (json) =>
          @companies = json
    ready: ->
      @getCompanies()
