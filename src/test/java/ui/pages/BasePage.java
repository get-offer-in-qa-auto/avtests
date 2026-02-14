package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import io.qameta.allure.Step;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.elements.BaseElement;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    /** Ключ для перехваченного текста window.alert() — headless Chrome не показывает native alert */
    private static final String ALERT_CAPTURE_KEY = "__lastAlert";

    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));

    public abstract String url();

    /** Перехват window.alert — в headless режиме native alert не работает */
    protected void injectAlertCapture() {
        executeJavaScript(
                "window." + ALERT_CAPTURE_KEY + " = null;" +
                "window.alert = function(msg) { window." + ALERT_CAPTURE_KEY + " = msg; };"
        );
    }

    @Step("Открыть страницу")
    public T open() {
        T page = Selenide.open(url(), (Class<T>) this.getClass());
        injectAlertCapture();
        return page;
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        injectAlertCapture();
        return Selenide.page(pageClass);
    }

    @Step("Проверить и принять алерт: {bankAlert}")
    public T checkAlertMessageAndAccept(String bankAlert) {
        // 1) Native alert (headed / Selenoid)
        Alert nativeAlert = tryGetNativeAlert(1);
        if (nativeAlert != null) {
            assertThat(nativeAlert.getText()).contains(bankAlert);
            nativeAlert.accept();
            return (T) this;
        }
        // 2) Перехваченный alert (headless + window.alert)
        Object raw = executeJavaScript("return window." + ALERT_CAPTURE_KEY + " || '';");
        String captured = raw == null ? "" : raw instanceof char[] ? new String((char[]) raw) : raw.toString();
        if (!captured.isBlank()) {
            assertThat(captured).contains(bankAlert);
            executeJavaScript("window." + ALERT_CAPTURE_KEY + " = null;");
            return (T) this;
        }
        // 3) Кастомный modal (React/Vue) — ищем текст в DOM (без эмодзи, т.к. приложение может их не показывать)
        String searchText = bankAlert.replaceAll("\\p{So}", "").trim();
        if (searchText.isEmpty()) searchText = bankAlert;
        SelenideElement msgEl = $(Selectors.withText(searchText)).shouldBe(Condition.visible, Duration.ofSeconds(4));
        assertThat(msgEl.getText()).contains(searchText);
        // Закрыть modal — кнопка в том же контейнере или любая видимая
        SelenideElement modal = msgEl.closest("[role='dialog'], [role='alertdialog'], .modal, .alert");
        if (modal.is(Condition.exist)) {
            modal.$$("button").filter(Condition.visible).first().click();
        } else {
            $$("button").filter(Condition.visible).first().click();
        }
        return (T) this;
    }

    private Alert tryGetNativeAlert(int timeoutSeconds) {
        try {
            return new WebDriverWait(webdriver().driver().getWebDriver(), Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.alertIsPresent());
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    // ElementCollection -> List<BaseElement>
    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }
}