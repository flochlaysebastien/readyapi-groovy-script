package readyapi.duplication


try {
    log.info("Duplicate image tests selector:")
    runTestStepByNameAndCheckResult("Check quicklook & thumbnail [automatic]", "Check quicklook & thumbnail")
    runTestStepByNameAndCheckResultWithCondition("Check quicklook & thumbnail [automatic]", "Check quicklook & thumbnail", context.expand( '${Properties#downloadLink}' )?.trim() ? true : false)
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
