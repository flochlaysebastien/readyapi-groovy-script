package sandbox

log.info(testRunner)
log.info(testRunner.testCase.testSuite.project.name)
log.info(testRunner.testCase.testSuite.project.getTestSuiteByName("Toolbox").getTestCases().size())
log.info(testRunner.testCase.testSuite.project.getTestSuiteByName("Toolbox").getTestCaseByName("log_versions").getTestSteps().size())
log.info(testRunner.testCase.testSuite.project.getTestSuiteByName("Toolbox").getTestCaseByName("log_versions").name)
log.info(testStepResult.getTestStep().name)
log.info(testStepResult.getStatus().toString())

/*
for ( testCaseResult in testRunner.getResults() )
{
	log.info(testCaseResult.getStatus().toString())

    // Getting all test cases’ names from the suite.
    testCaseName = testCaseResult.getTestCase().name
    log.info testCaseName

    // Checking whether the case has failed.
    if ( testCaseResult.getStatus().toString() == 'FAIL' )
    {
        // Logging failed cases and test steps’ resulting messages.
        log.info "$testCaseName has failed"
        for ( testStepResult in testCaseResult.getResults() )
        {
            testStepResult.messages.each() { msg -> log.info msg }
        }
    }

}
*/

// Index : https://www.soapui.org/apidocs/index-all.html
// WsdlProject : https://www.soapui.org/apidocs/com/eviware/soapui/impl/wsdl/WsdlProject.html
// WsdlTestSuite : https://www.soapui.org/apidocs/com/eviware/soapui/impl/wsdl/WsdlTestSuite.html
// WsdlTestCase : https://www.soapui.org/apidocs/com/eviware/soapui/impl/wsdl/testcase/WsdlTestCase.html
// TestSuiteRunner : https://www.soapui.org/apidocs/com/eviware/soapui/model/testsuite/TestSuiteRunner.html
// TestCaseRunner : https://www.soapui.org/apidocs/com/eviware/soapui/model/testsuite/TestCaseRunner.html
// TestStepResult : https://www.soapui.org/apidocs/com/eviware/soapui/model/testsuite/TestStepResult.html
// TestStep : https://www.soapui.org/apidocs/com/eviware/soapui/model/testsuite/TestStep.html
// TestStepStatus : https://www.soapui.org/apidocs/com/eviware/soapui/model/testsuite/TestStepResult.TestStepStatus.html