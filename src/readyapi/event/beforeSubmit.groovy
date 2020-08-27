package readyapi.event

import com.eviware.soapui.settings.HttpSettings
import com.eviware.soapui.SoapUI

def project
def requestName
try {
    project = submit.getRequest().getParent().getTestStep().getTestCase().getTestSuite().getProject();
    requestName = submit.getRequest().getParent().getTestStep().getTestCase().getTestSuite().getName() + "/" +
            submit.getRequest().getParent().getTestStep().getTestCase().getName() + "/" +
            submit.getRequest().getParent().getTestStep().getName()
} catch(Exception e) {
    project = submit.getRequest().getOperation().getInterface().getProject();
    requestName = submit.getRequest().getName();
}

def env = project.activeEnvironment.name;
def userRole = context.expand(submit.getRequest().getPropertyValue("userRole"));
if (!userRole) {
    userRole = "user";
}

log.debug(sprintf("# Sending a request as %s: %s", userRole, requestName))
log.debug("Set authentication password for " + userRole + "/" + env + " (Basic)");

def username = context.expand('${#Global#username.' + userRole + '.' + env + '}');
if (!username) {
    username = context.expand('${#Project#username.' + userRole + '}');
    if (!username) {
        log.error("Username not found. Please provide it in username." + userRole + " project property or username." + userRole + "." + env + " global property");
    } else {
        log.debug("Username is read from username." + userRole + " project property");
    }
} else {
    log.debug("Username is read from username." + userRole + "." + env + " global property");
}

def password = context.expand('${#Global#apikey.' + env + '.' + username + '}');
if (!password) {
    password = context.expand('${#Global#apikey.' + username + '}');
    if (!password) {
        log.error("Password not found. Please provide it in apikey." + username + " or apikey." + env + "." + username + " global properties");
    } else {
        log.debug("Password is read from apikey." + username + " global property");
    }
} else {
    log.debug("Password is read from apikey." + env + "." + username + " global property");
}

def testCaseName = ""
if (context.testCase != null) {
    testCaseName = context.testCase.name
}

def headers = submit.getRequest().getRequestHeaders()
if (testCaseName == "gcs_copy_file" || testCaseName == "gcs_delete_file") {
    def token = context.testCase.getPropertyValue("returnedToken")
    headers["Authorization"] = ["Bearer " + token]
} else {
    headers["Authorization"] = ["Basic " + ("APIKEY:" + password).bytes.encodeBase64().toString()]
}

log.debug("Authentication header is added");

if (testCaseName == "gcs_copy_file" || testCaseName == "gcs_delete_file") {
    // disable url-encode by readyapi
    def keystore = SoapUI.settings.getString(HttpSettings.ENCODED_URLS, "")
    if (keystore != "") {
        SoapUI.settings.setString( HttpSettings.ENCODED_URLS, "true" )
        context.testCase.setPropertyValue("returnedENCODED_URLS", keystore)
    } else {
        log.warn("HttpSettings.ENCODED_URLS is empty")
    }
}

def request = submit.getRequest()

if (requestName != "Toolbox/activity_wait_succeeded_status/Get activity status" && requestName != "Toolbox/neo_wait_for_activity") {
    endpoint = context.expand(request.getEndpoint())
    path = context.expand(request.getPath())
    if (path == endpoint){
        path = ""
    }
    log.info(sprintf("# Sending a request as %s: %s %s%s", userRole, request.getMethod(), endpoint, path))
}

request.setRequestHeaders(headers)