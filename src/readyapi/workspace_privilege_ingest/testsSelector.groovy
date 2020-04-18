package readyapi.workspace_privilege_ingest

try {
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