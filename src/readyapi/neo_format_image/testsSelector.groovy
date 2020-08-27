package readyapi.neo_format_image

try {
    log.info("Format image tests selector:")
    log.info("[${context.expand( '${Properties#returnedDownloadLink}' )}]")
    runTestStepByNameAndCheckResult("Check partial download [automatic]", "Get partial image")
    runTestStepByNameAndCheckResultWithCondition("Check image result [required]", "Check image result", context.expand('${Properties#checkImageResult}').toBoolean())
    runTestStepByNameAndCheckResultWithCondition("Check format parameters [required]", "Check format parameters", context.expand('${Properties#checkFormatParameters}').toBoolean())
    runTestStepByNameAndCheckResultWithCondition("Check catalog download links [required]", "Check catalog download links", context.expand('${Properties#checkDownloadLinks}').toBoolean())
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
