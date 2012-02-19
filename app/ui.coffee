class UI
    # runs the app!
    template = "service-list"
    ko.applyBindings(new ServiceModel("", "", 0, ""))