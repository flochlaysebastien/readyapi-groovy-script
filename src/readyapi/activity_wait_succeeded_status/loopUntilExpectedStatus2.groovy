package readyapi.activity_wait_succeeded_status

import groovy.time.*

try {
    TimeDuration duration
    Date timeStart = Date.parse("yyy-MM-dd'T'HH:mm:ss.SSSZ", context.expand('${Properties#timeStart}'))

    Integer timeout = context.expand('${Properties#timeout}').toInteger()
    Integer sleepTime = context.expand('${Properties#sleepTime}').toInteger()

    String expectedStatus = context.expand('${Properties#expectedStatus}')
    List<String> unexpectedStatusList = ["CANCELED", "ARCHIVED"]
    unexpectedStatusList.add(context.expand('${Properties#unexpectedStatus}'))
    unexpectedStatusList.sort()

    String previousStatus = context.expand('${Properties#previousStatus}')
    for (int i = 0; i < timeout; i += sleepTime) {
        String status = context.expand('${Properties#status}')
        if (status != previousStatus) {
            Date timeStop = new Date()
            duration = TimeCategory.minus(timeStop, timeStart)
            log.info("  + activity in [$status] status after ${duration}")
            previousStatus = status

            if (status == expectedStatus) {
                log.info("  :: ${context.expand('${Properties#type}')} activity end in ${duration}")
                testRunner.gotoStepByName("Property Transfer")
                return
            } else if (unexpectedStatusList.contains(status)) {
                throw new Exception("Activity differ from expected status")
            }
        }

        runTestStepByNameAndCheckResult("Get activity")
        runTestStepByNameAndCheckResult("Extract activity information")
        Thread.sleep(threadSleepTime)
    }

    Date timeStop = new Date()
    duration = TimeCategory.minus(timeStop, timeStart)
    log.info("  + activity fall in timeout status after ${duration}")
    throw new Exception("Timeout activity")
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to wait for activity")
}

void runTestStepByNameAndCheckResult(String stepName) throws Exception {
    def testStepResult = testRunner.runTestStepByName(stepName)
    if (!testStepResult.status.toString().equalsIgnoreCase("PASS")) {
        throw new Exception("Failed to run test step [$stepName]")
    }
}


//    String newDate = timeStart.format("yyy-MM-dd'T'HH:mm:ss.SSSZ")
//    def start = Date.parse("yyy-MM-dd'T'HH:mm:ss.SSSZ","2010-10-07T22:15:33.110+01:00".replace("+01:00","+0100"))
//    2020-05-29T11:17:09.350+0200