package readyapi.activity_wait_succeeded_status

import groovy.time.*

try {
    Date timeStart = new Date()
    TimeDuration duration

    Integer sleepTime = 3
    Integer threadSleepTime = sleepTime * 1000
    Integer timeout = context.expand('${Properties#timeout}').toInteger()

    String expectedStatus = context.expand('${Properties#expectedStatus}')
    List<String> unexpectedStatusList = ["CANCELED", "ARCHIVED"]
    unexpectedStatusList.add(context.expand('${Properties#unexpectedStatus}'))
    unexpectedStatusList.sort()

    String previousStatus
    Integer previousRound = 0
    for (int i = 0; i < timeout; i += sleepTime) {
        Integer round = i.intdiv(30)
        String status = context.expand('${Properties#status}')
        if (status != previousStatus) {
            duration = TimeCategory.minus(new Date(), timeStart)
            log.info("  + activity in [$status] status after ${duration}")
            previousStatus = status

            if (status == expectedStatus) {
                log.info("  :: ${context.expand('${Properties#type}')} activity end in ${duration}")
                testRunner.gotoStepByName("Property Transfer")
                return
            } else if (unexpectedStatusList.contains(status)) {
                throw new Exception("Activity differ from expected status")
            }
        } else if (round != previousRound) {
            previousRound = round
            duration = TimeCategory.minus(new Date(), timeStart)
            log.info("  + ${context.expand('${Properties#type}')} activity still in [$status] status after ${duration}")
        }

        runTestStepByNameAndCheckResult("Get activity")
        runTestStepByNameAndCheckResult("Extract activity information")
        Thread.sleep(threadSleepTime)
    }

    duration = TimeCategory.minus(new Date(), timeStart)
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