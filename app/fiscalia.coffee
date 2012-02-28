skeyid = 't5xC1SV2ISUiKfzIhPS2'

class Service extends Backbone.Model
	url: '/Action=CreateServiceSecurityKeyID=' + skeyid 
	defaults:
		ServicePrice: 0.0
	
	_create: (s, e) ->
		request = $.ajax 
						url: '/'
						data: 
							Action: 'CreateService'
							ServiceName: @.get 'ServiceName'
							ServiceVersion: @.get 'ServiceVersion'
							ServicePrice: @.get 'ServicePrice'
							SecurityKeyID: skeyid
		request.done s
		request.fail e
		
	sync: (method, model, options) ->
		switch method
			when "create" then  @_create(options.success, options.error)

		
class ServiceList extends Backbone.Collection
	model: Service

class ServiceListView extends Backbone.View
	tagName: 'div'
	el: $ '.adiv'
	events: 'click .btn' : 'addService'
	
	template: _.template($('#item-template').html())

	initialize: ->
		_.bindAll @
		@collection = new ServiceList
		@collection.bind 'add', @render
		@render()
		
	render: ->
		$(@el).html @template({services: @collection.toJSON()}) 
	
	addService: () ->
		service = new Service 
		
		service.save ServicePrice: $('#ServicePrice').val(), ServiceVersion: $('#ServiceVersion').val(), ServiceName: $('#ServiceName').val(),

					success: (model, resp) =>
									$(".modal-body p").html 'Created with Id: ' + resp.ServiceId
									$("#modal-create").modal 'toggle'
									@collection.add service
									
					error: (model, resp) -> 
									$(".modal-body p").html JSON.parse(resp.responseText).msg
									$("#modal-create").modal 'toggle'
		

