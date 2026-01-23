package ui;

import api.iteration2.BaseTest;
import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://host.docker.internal:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000; // 10 seconds timeout
        Configuration.pageLoadTimeout = 30000; // 30 seconds page load timeout

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
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
