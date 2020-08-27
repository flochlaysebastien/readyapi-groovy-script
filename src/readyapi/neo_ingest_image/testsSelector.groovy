package readyapi.neo_ingest_image

try {
    String returnedActivityId = context.expand('${Properties#returnedActivityId}')
    testRunner.testCase.testSteps["Properties"].setPropertyValue("cerebroCancelPayload", "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"activityID\":\"${returnedActivityId}\"}},{\"match\":{\"name\":\"cancel\"}}]}}}")
    testRunner.testCase.testSteps["Properties"].setPropertyValue("cerebroMonitoringPayload", "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"activityID\":\"${returnedActivityId}\"}},{\"match\":{\"name\":\"internalMonitoring\"}}]}}}")

    log.info("Ingest image tests selector:")
    runTestStepByNameAndCheckResult("Check activity information [automatic]", "Check activity information")
    runTestStepByNameAndCheckResult("Check cerebro cancel link [automatic]", "Check cerebro cancel link")
    runTestStepByNameAndCheckResult("Check cerebro monitoring link [automatic]", "Check cerebro monitoring link")


    runTestStepByNameAndCheckResultWithCondition("Check image histogram [automatic]", "Check image histogram", context.expand( '${Properties#imageMetadataId}' ) ? true : false)
    testRunner.testCase.testSteps["Properties"].setPropertyValue("imageMetadataId", context.expand( '${Properties#imageMetadataMultispectralId}' )?.trim().replaceAll(/[\[\]]/, ""))
    runTestStepByNameAndCheckResultWithCondition("Check multispectral image histogram [automatic]", "Check image histogram", context.expand( '${Properties#imageMetadataId}' ) ? true : false)
    testRunner.testCase.testSteps["Properties"].setPropertyValue("imageMetadataId", context.expand( '${Properties#imageMetadataPanchromaticId}' )?.trim().replaceAll(/[\[\]]/, ""))
    runTestStepByNameAndCheckResultWithCondition("Check panchromatic image histogram [automatic]", "Check image histogram", context.expand( '${Properties#imageMetadataId}' ) ? true : false)
    testRunner.testCase.testSteps["Properties"].setPropertyValue("imageMetadataId", context.expand( '${Properties#imageMetadataPansharpenId}' )?.trim().replaceAll(/[\[\]]/, ""))
    runTestStepByNameAndCheckResultWithCondition("Check pansharpen image histogram [automatic]", "Check image histogram", context.expand( '${Properties#imageMetadataId}' ) ? true : false)


    runTestStepByNameAndCheckResultWithCondition("Check streaming [required]", "Check streaming", context.expand('${Properties#checkStreaming}').toBoolean())
    runTestStepByNameAndCheckResultWithCondition("Check quicklook & thumbnail [required]", "Check quicklook & thumbnail", context.expand('${Properties#checkQuicklookThumbnail}').toBoolean())
    runTestStepByNameAndCheckResultWithCondition("Check download [required]", "Check download", context.expand('${Properties#checkDownload}').toBoolean())
    runTestStepByNameAndCheckResultWithCondition("Check download link absence [required]", "Check download link absence", context.expand('${Properties#checkDownloadLinkAbsence}').toBoolean())
    runTestStepByNameAndCheckResultWithCondition("Check correlationID [required]", "Check correlationID", context.expand('${Properties#checkCorrelationID}').toBoolean())
    testRunner.gotoStepByName("Property Transfer")
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to distribute tests")
}

void runTestStepByNameAndCheckResultWithCondition(String stepLog, String stepName, Boolean condition) throws Exception {
    if (condition) {
        runTestStepByNameAndCheckResult(stepLog, stepName)
    }
}

void runTestStepByNameAndCheckResult(String stepLog, String stepName) throws Exception {
    log.info("    - $stepLog")
    def testStepResult = testRunner.runTestStepByName(stepName)
    if (!testStepResult.status.toString().equalsIgnoreCase("PASS")) {
        throw new Exception("Failed to run test step [$stepName]")
    }
}
