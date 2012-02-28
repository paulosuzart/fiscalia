class FiscaAPI 
  

  remote : (reqdata)->
    @logger.debug "Starting request for #{reqdata.Action}"
    $.ajax
      url: "/"
      data:
        reqdata
     
  constructor: (@debugLevel)->
    @logger = new Logger @debugLevel
    @logger.info "Debug Level set to #{@debugLevel}"
  
  listServices: (next, onSucess) ->
    @logger.debug "Trying to get the list of services from #{next}"
    req = @remote
                Action: 'ListServices'
    req.done (data) => 
      @logger.info "Received #{data.length} services from server."
      @logger.debug "Calling onSucess"
      onSucess data
  