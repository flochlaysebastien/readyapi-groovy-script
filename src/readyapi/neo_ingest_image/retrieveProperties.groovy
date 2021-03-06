package readyapi.neo_ingest_image

try {
    logStepName()
    retrieveProperties()
    retrieveTestChoices()
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to prepare data")
}

void logStepName() {
    log.info("")
    log.info("#########################")
    log.info("####  ingest image")
    log.info("#########################")
    log.info("")
}

void retrieveProperties() {
    String isOrtho = testRunner.testCase.getPropertyValue("isOrtho")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("isOrtho", isOrtho ==~ "(yes)|(true)" ? 'true' : 'false')

    String optimize = testRunner.testCase.getPropertyValue("optimize")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("optimize", optimize ? optimize : "space")

    String nativeCopy = testRunner.testCase.getPropertyValue("nativeCopy")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("nativeCopy", nativeCopy ==~ "(yes)|(true)" ? 'true' : 'false')

    String expirationDate = context.expand('${#TestCase#expirationDate}')?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("expirationDate", expirationDate ? expirationDate : "1970-01-01T01:00:00.000Z")

    String forceDaasLight = testRunner.testCase.getPropertyValue("forceDaasLight")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("forceDaasLight", forceDaasLight ==~ "(yes)|(true)" ? 'true' : null)

    String metadata = testRunner.testCase.getPropertyValue("metadata")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("metadata", metadata ? metadata : "null")

    String properties = testRunner.testCase.getPropertyValue("properties")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("properties", properties ? properties : "{}")

    String userRole = context.expand('${#TestCase#userRole}')?.trim()?.toLowerCase()
    userRole = userRole ==~ "admin" ? 'admin' : 'impersonate'
    testRunner.testCase.testSteps["Properties"].setPropertyValue("userRole", userRole)
    if (userRole == "admin") {
        testRunner.testCase.testSteps["Properties"].setPropertyValue("forwardedUser", "")
        testRunner.gotoStepByName("Send ingestion order")
    }
}

void retrieveTestChoices() {
    Boolean isStreamingRequired = testRunner.testCase.getPropertyValue("checkStreaming")?.trim()?.toLowerCase() ==~ "(yes)|(true)"
    Boolean isOrtho = context.expand('${Properties#isOrtho}').toBoolean()
    Boolean isOptimizePerformance = context.expand('${Properties#optimize}') ==~ "performance"
    String checkStreaming = isStreamingRequired && (isOrtho || isOptimizePerformance) ? 'true' : 'false'
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkStreaming", checkStreaming)

    String checkQuicklookThumbnail = testRunner.testCase.getPropertyValue("checkQuicklookThumbnail")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkQuicklookThumbnail", checkQuicklookThumbnail ==~ "(yes)|(true)" ? 'true' : 'false')

    Boolean isNativeCopy = context.expand('${Properties#nativeCopy}').toBoolean()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkDownload", isNativeCopy.toString())
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkDownloadLinkAbsence", (!isNativeCopy).toString())

    String correlationID = testRunner.testCase.getPropertyValue("correlationID")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkCorrelationID", correlationID ? 'true' : 'false')
}