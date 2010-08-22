constructor("JQTcpTransportFactory", function(jobj = NULL) {
    extend(JObject(), "JQTcpTransportFactory", .jobj = jobj)
})

method("by", "JQTcpTransportFactory", enforceRCC = TRUE, function(static, ...) {
    JQTcpTransportFactory(jNew("jms/QTcpTransportFactory"))
})

