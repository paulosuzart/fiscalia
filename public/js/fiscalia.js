(function() {
  var FiscaAPI, Logger, Service, ServiceList, ServiceListView, skeyid,
    __hasProp = Object.prototype.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  Logger = (function() {
    var logLevel;

    logLevel = "DEBUG";

    function Logger(level) {
      logLevel = level;
    }

    Logger.prototype.doLog = function(level, msg) {
      if (logLevel === level) return console.log(msg);
    };

    Logger.prototype.debug = function(msg) {
      return this.doLog("DEBUG", msg);
    };

    Logger.prototype.info = function(msg) {
      return this.doLog("INFO", msg);
    };

    return Logger;

  })();

  FiscaAPI = (function() {

    FiscaAPI.prototype.remote = function(reqdata) {
      this.logger.debug("Starting request for " + reqdata.Action);
      return $.ajax({
        url: "/",
        data: reqdata
      });
    };

    function FiscaAPI(debugLevel) {
      this.debugLevel = debugLevel;
      this.logger = new Logger(this.debugLevel);
      this.logger.info("Debug Level set to " + this.debugLevel);
    }

    FiscaAPI.prototype.listServices = function(next, onSucess) {
      var req,
        _this = this;
      this.logger.debug("Trying to get the list of services from " + next);
      req = this.remote({
        Action: 'ListServices'
      });
      return req.done(function(data) {
        _this.logger.info("Received " + data.length + " services from server.");
        _this.logger.debug("Calling onSucess");
        return onSucess(data);
      });
    };

    return FiscaAPI;

  })();

  skeyid = 't5xC1SV2ISUiKfzIhPS2';

  Service = (function(_super) {

    __extends(Service, _super);

    function Service() {
      Service.__super__.constructor.apply(this, arguments);
    }

    Service.prototype.url = '/Action=CreateServiceSecurityKeyID=' + skeyid;

    Service.prototype.defaults = {
      ServicePrice: 0.0
    };

    Service.prototype._create = function(s, e) {
      var request;
      request = $.ajax({
        url: '/',
        data: {
          Action: 'CreateService',
          ServiceName: this.get('ServiceName'),
          ServiceVersion: this.get('ServiceVersion'),
          ServicePrice: this.get('ServicePrice'),
          SecurityKeyID: skeyid
        }
      });
      request.done(s);
      return request.fail(e);
    };

    Service.prototype.sync = function(method, model, options) {
      switch (method) {
        case "create":
          return this._create(options.success, options.error);
      }
    };

    return Service;

  })(Backbone.Model);

  ServiceList = (function(_super) {

    __extends(ServiceList, _super);

    function ServiceList() {
      ServiceList.__super__.constructor.apply(this, arguments);
    }

    ServiceList.prototype.model = Service;

    return ServiceList;

  })(Backbone.Collection);

  ServiceListView = (function(_super) {

    __extends(ServiceListView, _super);

    function ServiceListView() {
      ServiceListView.__super__.constructor.apply(this, arguments);
    }

    ServiceListView.prototype.tagName = 'div';

    ServiceListView.prototype.el = $('.adiv');

    ServiceListView.prototype.events = {
      'click .btn': 'addService'
    };

    ServiceListView.prototype.template = _.template($('#item-template').html());

    ServiceListView.prototype.initialize = function() {
      _.bindAll(this);
      this.collection = new ServiceList;
      this.collection.bind('add', this.render);
      return this.render();
    };

    ServiceListView.prototype.render = function() {
      return $(this.el).html(this.template({
        services: this.collection.toJSON()
      }));
    };

    ServiceListView.prototype.addService = function() {
      var service,
        _this = this;
      service = new Service;
      return service.save({
        ServicePrice: $('#ServicePrice').val(),
        ServiceVersion: $('#ServiceVersion').val(),
        ServiceName: $('#ServiceName').val()
      }, {
        success: function(model, resp) {
          $(".modal-body p").html('Created with Id: ' + resp.ServiceId);
          $("#modal-create").modal('toggle');
          return _this.collection.add(service);
        },
        error: function(model, resp) {
          $(".modal-body p").html(JSON.parse(resp.responseText).msg);
          return $("#modal-create").modal('toggle');
        }
      });
    };

    return ServiceListView;

  })(Backbone.View);

  jQuery(function() {
    var FiscaliaApp, api, init;
    api = new FiscaAPI("DEBUG");
    FiscaliaApp = (function(_super) {

      __extends(FiscaliaApp, _super);

      function FiscaliaApp() {
        FiscaliaApp.__super__.constructor.apply(this, arguments);
      }

      FiscaliaApp.prototype.routes = {
        "services": "showService"
      };

      FiscaliaApp.prototype.showService = function() {
        var serviceList_view, services;
        serviceList_view = new ServiceListView;
        return services = api.listServices(0, function(data) {
          return serviceList_view.collection.add(data);
        });
      };

      return FiscaliaApp;

    })(Backbone.Router);
    init = function() {
      var app;
      app = new FiscaliaApp;
      Backbone.history.start({
        pushState: true
      });
      return app.navigate("index.html#services");
    };
    return $(document).ready(init);
  });

}).call(this);
