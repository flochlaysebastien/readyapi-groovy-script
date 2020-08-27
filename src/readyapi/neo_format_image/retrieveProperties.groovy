package readyapi.neo_format_image

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
    log.info("####  format image")
    log.info("#########################")
    log.info("")
}

void retrieveProperties() {
    String executionOrder = testRunner.testCase.getPropertyValue("executionOrder")?.trim()
    if (executionOrder) {
        testRunner.testCase.testSteps["Properties"].setPropertyValue("type", null)
        testRunner.testCase.testSteps["Properties"].setPropertyValue("compression", null)
        testRunner.testCase.testSteps["Properties"].setPropertyValue("imageFormat", null)
        testRunner.testCase.testSteps["Properties"].setPropertyValue("checkFormatParameters", "false")
    } else {
        String type
        String compression
        String imageFormat
        String mippConfiguration = context.expand('${#TestCase#mippConfiguration}')

        String formatProfile = testRunner.testCase.getPropertyValue("formatProfile")?.trim()?.toLowerCase()
        formatProfile = formatProfile ? formatProfile : 'jp2-v2'

        switch (formatProfile) {
            case 'jp2-v2':
                type = 'dimapV2'
                compression = 'zip'
                imageFormat = 'image/jp2'
                break;
            case 'jp2-v2-tar':
                type = 'dimapV2'
                compression = 'tar'
                imageFormat = 'image/jp2'
                break;
            case 'geotiff-v2':
                type = 'dimapV2'
                compression = 'zip'
                imageFormat = 'image/geotiff'
                break;
            case 'jp2-v3':
                type = 'dimapV2-pleiadesneo'
                compression = 'zip'
                imageFormat = 'image/jp2'
                break;
            default:
                throw new Exception("No corresponding format profile. [$formatProfile]")
        }

        testRunner.testCase.testSteps["Properties"].setPropertyValue("type", type)
        testRunner.testCase.testSteps["Properties"].setPropertyValue("compression", compression)
        testRunner.testCase.testSteps["Properties"].setPropertyValue("imageFormat", imageFormat)
        testRunner.testCase.testSteps["Properties"].setPropertyValue("checkFormatParameters", "true")
        executionOrder = computeExecutionOrder(type, imageFormat, mippConfiguration, compression)
    }
    testRunner.testCase.testSteps["Properties"].setPropertyValue("executionOrder", executionOrder)

    String priority = testRunner.testCase.getPropertyValue("priority")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("priority", priority ? priority : "42")

    String userRole = context.expand('${#TestCase#userRole}')?.trim()?.toLowerCase()
    userRole = userRole ==~ "admin" ? 'admin' : 'impersonate'
    testRunner.testCase.testSteps["Properties"].setPropertyValue("userRole", userRole)
    if (userRole == "admin") {
        testRunner.testCase.testSteps["Properties"].setPropertyValue("forwardedUser", "")
        testRunner.gotoStepByName("Start format order")
    }
}

void retrieveTestChoices() {
    String referenceFilePath = testRunner.testCase.getPropertyValue("referenceFilePath")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkImageResult", referenceFilePath ? 'true' : 'false')

    String catalogLink = testRunner.testCase.getPropertyValue("catalogLink")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkDownloadLinks", catalogLink ? 'true' : 'false')

    String correlationID = testRunner.testCase.getPropertyValue("correlationID")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkCorrelationID", correlationID ? 'true' : 'false')
}

String computeExecutionOrder(String type, String imageFormat, String mippConfiguration, String compression) {
    String executionOrder = """{
   "packaging":{
      "type":"${type}",
      "parameters":{
         "imageFormat":"${imageFormat}",
         "mippConfiguration":"${mippConfiguration}"
      }
   },
   "compression":"${compression}"
}"""
    return executionOrder
}