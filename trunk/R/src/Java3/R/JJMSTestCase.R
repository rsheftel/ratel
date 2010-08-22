constructor("JJMSTestCase", function(jobj = NULL) {
    extend(JObject(), "JJMSTestCase", .jobj = jobj)
})

method("by", "JJMSTestCase", enforceRCC = TRUE, function(static, ...) {
    JJMSTestCase(jNew("jms/JMSTestCase"))
})

method("tearDown", "JJMSTestCase", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "tearDown")
})

method("useTestBroker", "JJMSTestCase", enforceRCC = TRUE, function(static, ...) {
    jCall("jms/JMSTestCase", "V", "useTestBroker")
})

method("staticSetup", "JJMSTestCase", enforceRCC = TRUE, function(static, ...) {
    jCall("jms/JMSTestCase", "V", "staticSetup")
})

method("setUp", "JJMSTestCase", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "setUp")
})

method("TEST_BROKER2", "JJMSTestCase", enforceRCC = FALSE, function(static, ...) {
    lazy(JJMSTestCase$...TEST_BROKER2, jField("jms/JMSTestCase", "Ljava/lang/String;", "TEST_BROKER2"), log = FALSE)
})

method("TEST_BROKER", "JJMSTestCase", enforceRCC = FALSE, function(static, ...) {
    lazy(JJMSTestCase$...TEST_BROKER, jField("jms/JMSTestCase", "Ljava/lang/String;", "TEST_BROKER"), log = FALSE)
})

