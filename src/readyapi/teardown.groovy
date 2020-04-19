package readyapi

for ( testStepResult in testRunner.getResults() ){
    if(testStepResult.getStatus().toString() ==  'FAIL'){
        testRunner.testCase.testSuite.project.getTestSuiteByName("Toolbox").getTestCaseByName("log_versions").run(null, false)
        return
    }
}