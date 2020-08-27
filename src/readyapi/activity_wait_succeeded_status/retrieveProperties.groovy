package readyapi.activity_wait_succeeded_status

try {
    retrieveProperties()
    logStepName()
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to prepare data")
}

void logStepName() {
    log.info("Wait for a [${context.expand('${Properties#expectedStatus}')}] activity :")
    log.info("  :: type : ${context.expand('${Properties#type}')}")
    log.info("  :: subtype : ${context.expand('${Properties#subtype}')}")
    log.info("  :: id : ${context.expand('${Properties#id}')}")
    log.info("  :: timeout : ${context.expand('${Properties#timeout}')} seconds")
}

void retrieveProperties() {
    Date timeStart = new Date()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("timeStart", timeStart.format("yyy-MM-dd'T'HH:mm:ss.SSSZ"))

    Integer timeout = getIntegerOrDefault(testRunner.testCase.getPropertyValue("timeout"), 10800)
    testRunner.testCase.testSteps["Properties"].setPropertyValue("timeout", timeout.toString())

    String expectedStatus
    String unexpectedStatus
    String isSuccessfulExpected = testRunner.testCase.getPropertyValue("isSuccessfulExpected")?.trim()?.toLowerCase()
    if (isSuccessfulExpected ==~ "(no)|(false)" ? false : true) {
        expectedStatus = "SUCCEEDED"
        unexpectedStatus = "FAILED"
    } else {
        expectedStatus = "FAILED"
        unexpectedStatus = "SUCCEEDED"
    }
    testRunner.testCase.testSteps["Properties"].setPropertyValue("expectedStatus", expectedStatus)
    testRunner.testCase.testSteps["Properties"].setPropertyValue("unexpectedStatus", unexpectedStatus)

    Integer sleepTime = 1
    testRunner.testCase.testSteps["Properties"].setPropertyValue("sleepTime", sleepTime.toString())
    testRunner.testCase.testSteps["Properties"].setPropertyValue("threadSleepTime", (sleepTime * 1000).toString())

    testRunner.testCase.testSteps["Properties"].setPropertyValue("previousStatus", "")
    testRunner.testCase.testSteps["Properties"].setPropertyValue("previousStatus", "")
}

Integer getIntegerOrDefault(String expression, Integer defaultValue) {
    try {
        return expression.toInteger()
    } catch (Exception ex) {
        return defaultValue
    }
}
