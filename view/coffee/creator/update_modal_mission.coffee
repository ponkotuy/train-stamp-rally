
@updateModal = (el) -> new Vue
  el: el
  data:
    mission: {}
    introduction: ''
    clearText: ''
  methods:
    setMission: (mission) ->
      @mission = mission
      @introduction = mission.introduction
      @clearText = mission.clearText
    update: ->
      API.putJSON
        url: "/api/mission/#{@mission.id}"
        data:
          introduction: @introduction
          clearText: @clearText
        success:
          location.reload(false)
