package ui.extensions;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.chromium.HasCdp;

import java.util.Map;

/**
 * Инжектит перехват window.alert через CDP до загрузки страницы.
 * Headless Chrome не показывает native alert — скрипт сохраняет текст в window.__lastAlert.
 * Должен выполняться первым, до AdminSessionExtension/UserSessionExtension.
 */
public class AlertCaptureCdpExtension implements BeforeEachCallback {

    private static final String ALERT_CAPTURE_SCRIPT =
            "window.__lastAlert = null; window.alert = function(msg) { window.__lastAlert = msg; };";

    @Override
    public void beforeEach(ExtensionContext context) {
        // Создаём драйвер и грузим пустую страницу, чтобы вызвать CDP до любой навигации к приложению
        Selenide.open("about:blank");
        var driver = WebDriverRunner.getWebDriver();
        if (driver instanceof HasCdp cdp) {
            cdp.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", ALERT_CAPTURE_SCRIPT));
        }
    }
}
