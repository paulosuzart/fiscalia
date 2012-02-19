class ServiceModel
    skeyid = "t5xC1SV2ISUiKfzIhPS2"
    
    constructor: (name, version, price, notes) -> 
        @name = ko.observable name
        @version = ko.observable version
        @price = ko.observable price
        @createService.bind @

    mkRequest: (action) ->
        $.ajax 
          url: '/'
          data: 
                Action: action
                ServiceName: @name()
                ServiceVersion: @version()
                ServicePrice: @price()
                SecurityKeyID: @skeyid

    createService: ->
        request = @mkRequest('CreateService')
        request.done (data) ->
                    $(".modal-body p").html 'Created with Id: ' + data.ServiceId
                    $("#modal-create").modal 'toggle'

        request.fail (jqHXT, result, thrown) ->
                    $(".modal-body p").html JSON.parse(jqHXT.responseText).msg
                    $("#modal-create").modal 'toggle'


