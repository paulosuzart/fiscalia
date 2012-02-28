class Logger

  logLevel = "DEBUG"	

  constructor: (level)->
    logLevel = level
  
  doLog: (level, msg)->
    console.log msg if logLevel is level
    
  debug: (msg)->
    @doLog "DEBUG", msg
  
  info: (msg)->
    @doLog "INFO", msg