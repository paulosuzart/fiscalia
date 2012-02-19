(function() {
  var UI,
    __hasProp = Object.prototype.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  jQuery(function() {
    var Service, ServiceList, ServiceListView, init, skeyid;
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
    init = function() {
      var serviceList_view;
      serviceList_view = new ServiceListView;
      return serviceList_view.collection.add([
        new Service({
          ServicePrice: 0.1,
          ServiceName: 'MortgageService',
          ServiceVersion: 'V1_0'
        }), new Service({
          ServicePrice: 0.2,
          ServiceName: 'SampleS',
          ServiceVersion: '1.0'
        })
      ]);
    };
    return $(document).ready(init);
  });

  UI = (function() {
    var template;

    function UI() {}

    template = "service-list";

    ko.applyBindings(new ServiceModel("", "", 0, ""));

    return UI;

  })();

}).call(this);
