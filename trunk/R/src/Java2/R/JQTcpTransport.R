constructor("JQTcpTransport", function(jobj = NULL) {
    extend(JObject(), "JQTcpTransport", .jobj = jobj)
})

method("by_WireFormat_SocketFactory_URI_URI", "JQTcpTransport", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, ...) {
    JQTcpTransport(jNew("jms/QTcpTransport", .jcast(j_arg0$.jobj, "org.apache.activemq.wireformat.WireFormat"), .jcast(j_arg1$.jobj, "javax.net.SocketFactory"), .jcast(j_arg2$.jobj, "java.net.URI"), .jcast(j_arg3$.jobj, "java.net.URI")))
})

