package readyapi.neo_ingest_image

try {
    String returnedActivityId = context.expand( '${Properties#returnedActivityId}' )
    testRunner.testCase.testSteps["Properties"].setPropertyValue("cerebroCancelPayload", "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"activityID\":\"${returnedActivityId}\"}},{\"match\":{\"name\":\"cancel\"}}]}}}")
    testRunner.testCase.testSteps["Properties"].setPropertyValue("cerebroMonitoringPayload", "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"activityID\":\"${returnedActivityId}\"}},{\"match\":{\"name\":\"internalMonitoring\"}}]}}}")

    log.info("Ingest image tests selector:")
    log.info("    - Check activity information [automatic]")
    testRunner.runTestStepByName("Check activity information")

    log.info("    - Check cerebro cancel link [automatic]")
    testRunner.runTestStepByName("Check cerebro cancel link")

    log.info("    - Check cerebro monitoring link [automatic]")
    testRunner.runTestStepByName("Check cerebro monitoring link")

    if (context.expand('${Properties#checkStreaming}').toBoolean()) {
        log.info("    - Check streaming [required]")
        testRunner.runTestStepByName("Check streaming")
    }

    if (context.expand('${Properties#checkQuicklookThumbnail}').toBoolean()) {
        log.info("    - Check quicklook & thumbnail [required]")
        testRunner.runTestStepByName("Check quicklook & thumbnail")
    }

    if (context.expand('${Properties#checkDownload}').toBoolean()) {
        log.info("    - Check download [required]")
        testRunner.runTestStepByName("Check download")
    }

    if (context.expand('${Properties#checkDownloadLinkAbsence}').toBoolean()) {
        log.info("    - Check download link absence [required]")
        testRunner.runTestStepByName("Check download link absence")
    }

    if (context.expand('${Properties#checkCorrelationID}').toBoolean()) {
        log.info("    - Check correlationID [required]")
        testRunner.runTestStepByName("Check correlationID")
    }

    testRunner.gotoStepByName("Property Transfer")
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to distribute tests")
}