constructor("JLiveOrders", function(jobj = NULL) {
    extend(JObject(), "JLiveOrders", .jobj = jobj)
})

method("by", "JLiveOrders", enforceRCC = TRUE, function(static, ...) {
    JLiveOrders(jNew("systemdb/metadata/LiveOrders"))
})

method("order_by_int", "JLiveOrders", enforceRCC = TRUE, function(this, id = NULL, ...) {
    JLiveOrder(jobj = jCall(this$.jobj, "Lsystemdb/metadata/LiveOrders/LiveOrder;", "order", theInteger(id)))
})

method("prefixes", "JLiveOrders", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "prefixes"))
})

method("ordersAfter_by_int", "JLiveOrders", enforceRCC = TRUE, function(this, greaterThanMe = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "ordersAfter", theInteger(greaterThanMe)))
})

method("maxIdBeforeToday", "JLiveOrders", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "maxIdBeforeToday")
})

method("ordersFilled_by_int_String", "JLiveOrders", enforceRCC = TRUE, function(this, systemId = NULL, market = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "ordersFilled", theInteger(systemId), the(market)))
})

method("ordersSubmitted_by_int_String", "JLiveOrders", enforceRCC = TRUE, function(this, systemId = NULL, market = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "ordersSubmitted", theInteger(systemId), the(market)))
})

method("insert_by_int_String_Date_Date_String_String_long_Double_String_String_String_String_String", "JLiveOrders", enforceRCC = TRUE, function(this, systemId = NULL, market = NULL, filledAt = NULL, submittedAt = NULL, entryExit = NULL, positionDirection = NULL, size = NULL, price = NULL, details = NULL, description = NULL, hostname = NULL, topicPrefix = NULL, ferretOrderId = NULL, ...) {
    jCall(this$.jobj, "I", "insert", theInteger(systemId), the(market), .jcast(filledAt$.jobj, "java.util.Date"), .jcast(submittedAt$.jobj, "java.util.Date"), the(entryExit), the(positionDirection), theLong(size), .jcast(price$.jobj, "java.lang.Double"), the(details), the(description), the(hostname), the(topicPrefix), the(ferretOrderId))
})

method("ORDERS", "JLiveOrders", enforceRCC = FALSE, function(static, ...) {
    lazy(JLiveOrders$...ORDERS, JLiveOrders(jobj = jField("systemdb/metadata/LiveOrders", "Lsystemdb/metadata/LiveOrders;", "ORDERS")), log = FALSE)
})

