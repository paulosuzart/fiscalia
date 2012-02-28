jQuery ->
  api = new FiscaAPI("DEBUG")
  
  class FiscaliaApp extends Backbone.Router
    
    routes: { "services" : "showService" }

    showService: () ->
      serviceList_view = new ServiceListView
      services = api.listServices(0, (data) -> serviceList_view.collection.add data)
      #serviceList_view.collection.add services

  init = ->
    app = new FiscaliaApp
    Backbone.history.start {pushState: true}
    app.navigate "index.html#services"
  
  $(document).ready init

    