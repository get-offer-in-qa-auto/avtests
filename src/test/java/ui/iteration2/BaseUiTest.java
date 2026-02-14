package ui.iteration2;

import api.common.extensions.AdminSessionExtension;
import api.common.extensions.BrowserMatchExtension;
import api.common.extensions.UserSessionExtension;
import api.configs.Config;
import api.iteration2.BaseTest;
import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import ui.listeners.ScreenshotOnStepListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        String uiRemote = Config.getProperty("uiRemote");
        boolean useLocalBrowser = uiRemote == null || uiRemote.isBlank() || "local".equalsIgnoreCase(uiRemote.trim());

        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true)
                .includeSelenideSteps(false));
        SelenideLogger.addListener("ScreenshotOnStep", new ScreenshotOnStepListener());

        if (useLocalBrowser) {
            Configuration.remote = null;
            Configuration.baseUrl = Config.getProperty("uiBaseUrl");
            Configuration.headless = true;
            // CI медленнее — увеличиваем таймауты (GITHUB_ACTIONS задаётся автоматически)
            if ("true".equals(System.getenv("GITHUB_ACTIONS"))) {
                Configuration.timeout = 15_000;
                Configuration.pageLoadTimeout = 30_000;
            }
        } else {
            Configuration.remote = uiRemote;
            String forBrowsers = Config.getProperty("uiBaseUrlForBrowsers");
            Configuration.baseUrl = (forBrowsers != null && !forBrowsers.isBlank()) ? forBrowsers : Config.getProperty("uiBaseUrl");
            Configuration.browserCapabilities.setCapability("selenoid:options",
                    Map.of("enableVNC", true, "enableLog", true));
        }
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