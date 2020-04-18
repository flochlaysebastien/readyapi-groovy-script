package readyapi.workspace_privilege_ingest

try {
    retrieveProperties()
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to prepare data")
}

void retrieveProperties() {
    String workspaceCustomRights = testRunner.testCase.getPropertyValue("workspaceCustomRights")?.trim()?.toLowerCase()
    if (!workspaceCustomRights) {
        workspaceCustomRights = '{"browse":{},"create":{},"delete":{},"wmts":{},"wms":{},"buffer":{},"download":{},"associatedData":{}}'
    }
    testRunner.testCase.testSteps["Properties"].setPropertyValue("privilegesRights", workspaceCustomRights)
}