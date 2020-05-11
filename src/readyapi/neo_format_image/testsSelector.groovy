package readyapi.neo_format_image

try {
    log.info("Format image tests selector:")

    if (context.expand('${Properties#checkImageResult}').toBoolean()) {
        log.info("    - Check image result [required]")
        testRunner.runTestStepByName("Check image result")
    }

    if (context.expand('${Properties#checkFormatParameters}').toBoolean()) {
        log.info("    - Check format parameters [required]")
        testRunner.runTestStepByName("Check format parameters")
    }

    if (context.expand('${Properties#checkDownloadLinks}').toBoolean()) {
        log.info("    - Check catalog download links [required]")
        testRunner.runTestStepByName("Check catalog download links")
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