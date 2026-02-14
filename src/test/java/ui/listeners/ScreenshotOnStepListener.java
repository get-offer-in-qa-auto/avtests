package ui.listeners;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;

import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * Прикрепляет скриншот страницы к каждому шагу UI-теста.
 */
public class ScreenshotOnStepListener implements LogEventListener {

    @Override
    public void beforeEvent(LogEvent event) {
        // no-op
    }

    @Override
    public void afterEvent(LogEvent event) {
        if (event.getStatus() == LogEvent.EventStatus.PASS) {
            getScreenshotBytes().ifPresent(bytes ->
                    Allure.getLifecycle().addAttachment("Скриншот", "image/png", "png",
                            new ByteArrayInputStream(bytes)));
        }
    }

    private static Optional<byte[]> getScreenshotBytes() {
        try {
            if (WebDriverRunner.hasWebDriverStarted() && WebDriverRunner.getWebDriver() instanceof TakesScreenshot driver) {
                return Optional.of(driver.getScreenshotAs(OutputType.BYTES));
            }
        } catch (WebDriverException ignored) {
            // браузер может быть ещё не готов
        }
        return Optional.empty();
    }
}
