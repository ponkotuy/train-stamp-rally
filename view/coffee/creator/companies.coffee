$(document).ready ->
  new Vue
    el: '#companies'
    data:
      companies: []
      modal: null
    methods:
      getCompanies: ->
        API.getJSON '/api/companies', (json) =>
          API.getJSON '/api/companies/exists_fare', (fares) =>
            @companies = json.map (company) ->
              types = fares.filter (fare) -> fare.companyId == company.id
                .map (fare) -> fare.trainType
              company['types'] = types
              company
      openModal: (company, trainType)->
        @modal.setFareType(company, trainType)
        $(modalId).modal('show')
    ready: ->
      @getCompanies()
      @modal = fareModal()

modalId = '#fareModal'
fareModal = ->
  new Vue
    el: modalId
    data:
      company: {}
      trainType: {}
      fares: []
    methods:
      setFareType: (company, trainType) ->
        @company = company
        @trainType = trainType
        @getFares()
      getFares: ->
        API.getJSON "/api/company/#{@company.id}/type/#{@trainType.value}/fares", (json) =>
          @fares = json
