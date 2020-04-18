package readyapi

import groovy.json.JsonSlurper

try {
    generateName()
    generateTitle()
    generateLabelList()
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to prepare data for workspace creation")
}

/*
Due to the complexity to generate a unique workspace name during parallel tests run,
it's necessary to lean on a unique id. We use the correlation id from the previous step to build it.
 */
void generateName() throws Exception {
    String correlationId

    // get X-Correlation-Id header value from previous step
    JsonSlurper jsonSlurper = new groovy.json.JsonSlurper()
    def response = jsonSlurper.parseText(context.expand('${Get user id#HarResponse#$[\'headers\']}'))
    response.each {
        if (it.name == "X-Correlation-Id") {
            correlationId = it.value
            return
        }
    }

    // fail if correlationId is empty
    if (!correlationId?.trim()) {
        throw new Exception("Failed to create a unique workspace name. [No correlationId found]")
    }

    // generate unique workspace name with correlation Id which do not match UUID v4 regex
    uniqueWorkspaceName = "r-" + correlationId.take(18)
    testRunner.testCase.testSteps["Properties"].setPropertyValue("workspaceName", uniqueWorkspaceName)
}

void generateTitle() throws Exception {
    title = testRunner.testCase.getPropertyValue("title")
    if (!title?.trim()) {
        title = "${context.expand( '${Properties#workspaceName}' )} title"
    }

    testRunner.testCase.testSteps["Properties"].setPropertyValue("workspaceTitle", title)
}

void generateLabelList() throws Exception {
    List<String> labelList = ['"ready-api"']

    String labels = testRunner.testCase.getPropertyValue("labelList")
    if (labels?.trim()) {
        new groovy.json.JsonSlurper().parseText(labels).each {
            labelList.push("\"$it\"")
        }
    }

    testRunner.testCase.testSteps["Properties"].setPropertyValue("workspaceLabels", labelList.toString())
}