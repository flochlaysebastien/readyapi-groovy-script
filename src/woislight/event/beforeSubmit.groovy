import com.eviware.soapui.settings.HttpSettings
import com.eviware.soapui.SoapUI

try {
    def request = submit.getRequest()
    def project = request.getParent().getProject()
    String environment = project.activeEnvironment.name;
    String userRole = request.getPropertyValue("userRole");
    if (!userRole) {
        userRole = "user";
    }

    String authKey = sprintf('${#Global#userRole.%s.%s}', environment.toLowerCase(), userRole.toLowerCase())
    String authorization = context.expand(authKey)
    if (!authorization) {
        log.error(sprintf("authorization not found. Please provide it in Global properties. The expected propertiy is [%s]", authKey));
    }

    //def headers = submit.getRequest().getRequestHeaders()
    def headers = request.getRequestHeaders()
    headers["Authorization"] = [authorization]
    headers["Host"] = ["request-wois-light-dev.idp.private.geoapi-airbusds.com"]
    request.setRequestHeaders(headers)
    //submit.getRequest().setRequestHeaders(headers)
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to prepare authorization header")
}