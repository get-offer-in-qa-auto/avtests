package ui.iteration2;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import api.common.extensions.AdminSessionExtension;
import api.common.extensions.BrowserMatchExtension;
import api.common.extensions.UserSessionExtension;
import api.iteration2.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
@Execution(ExecutionMode.CONCURRENT) 
public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = api.configs.Config.getProperty("uiRemote");
        // Use Config helper method to get URL suitable for browser containers
        Configuration.baseUrl = api.configs.Config.getUiBaseUrlForBrowsers();
        Configuration.browser = api.configs.Config.getProperty("browser");
        Configuration.browserSize = api.configs.Config.getProperty("browserSize");
        Configuration.timeout = 15000; // 15 seconds timeout for element operations
        Configuration.pageLoadTimeout = 60000; // 60 seconds page load timeout
        

        // Configure Selenoid options
        Map<String, Object> selenoidOptions = new HashMap<>();
        selenoidOptions.put("enableVNC", true);
        selenoidOptions.put("enableLog", true);
        
        Configuration.browserCapabilities.setCapability("selenoid:options", selenoidOptions);
        
        Configuration.remoteConnectionTimeout = 60000; 
        Configuration.remoteReadTimeout = 120000;
    }

    public void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }
}
