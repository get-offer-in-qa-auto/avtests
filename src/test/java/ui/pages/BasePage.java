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

    /** Перехват window.alert — в headless режиме native alert не работает. Не сбрасывать __lastAlert — иначе затрём перехваченное сообщение. */
    protected void injectAlertCapture() {
        executeJavaScript("window.alert = function(msg) { window." + ALERT_CAPTURE_KEY + " = msg; };");
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
        // НЕ вызывать injectAlertCapture() здесь — он сбрасывает __lastAlert в null и затирает перехваченное сообщение!
        // 1) Native alert (headed / Selenoid)
        Alert nativeAlert = tryGetNativeAlert(2);
        if (nativeAlert != null) {
            assertThat(nativeAlert.getText()).contains(bankAlert);
            nativeAlert.accept();
            return (T) this;
        }
        // 2) Перехваченный alert (headless + window.alert) — опрос, т.к. callback может прийти асинхронно
        // В CI API может отвечать медленнее — 15s на перехват
        String captured = pollForCapturedAlert(15);
        if (!captured.isBlank()) {
            assertThat(captured).contains(bankAlert);
            executeJavaScript("window." + ALERT_CAPTURE_KEY + " = null;");
            return (T) this;
        }
        // 3) Кастомный modal (React/Vue) — ищем по короткому ключу (текст может быть разбит по DOM)
        String searchText = bankAlert.replaceAll("\\p{So}", "").trim();
        if (searchText.isEmpty()) searchText = bankAlert;
        String searchKey = extractSearchKey(searchText);
        long timeoutSec = Math.max(15, com.codeborne.selenide.Configuration.timeout / 1000L);
        SelenideElement msgEl = $(Selectors.withText(searchKey)).shouldBe(Condition.visible, Duration.ofSeconds(timeoutSec));
        assertThat(msgEl.getText()).as("Modal message").contains(searchKey);
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

    /** Извлекаем короткий ключ для поиска — DOM может разбивать длинный текст */
    private String extractSearchKey(String fullText) {
        if (fullText.length() <= 25) return fullText;
        if (fullText.contains("New Account Created")) return "New Account Created";
        if (fullText.contains("Successfully deposited")) return "Successfully deposited";
        if (fullText.contains("Successfully transferred")) return "Successfully transferred";
        if (fullText.contains("Name updated")) return "Name updated";
        if (fullText.contains("User created")) return "User created";
        if (fullText.contains("Username must be")) return "Username must be";
        if (fullText.contains("Name must contain")) return "Name must contain";
        if (fullText.contains("Please enter a valid name")) return "Please enter a valid name";
        if (fullText.contains("Please deposit less")) return "Please deposit less";
        if (fullText.contains("Error: Invalid transfer")) return "Error: Invalid transfer";
        if (fullText.contains("Error: Transfer amount")) return "Error: Transfer amount";
        return fullText.substring(0, Math.min(30, fullText.length()));
    }

    /** Опрос __lastAlert — callback alert() может сработать асинхронно после клика */
    private String pollForCapturedAlert(int maxWaitSeconds) {
        for (int i = 0; i < maxWaitSeconds * 5; i++) {
            Object raw = executeJavaScript("return window." + ALERT_CAPTURE_KEY + " || '';");
            String s = raw == null ? "" : raw instanceof char[] ? new String((char[]) raw) : raw.toString();
            if (!s.isBlank()) return s;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return "";
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        executeJavaScript("window.alert = function(msg) { window." + ALERT_CAPTURE_KEY + " = msg; };");
    }

    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    // ElementCollection -> List<BaseElement>
    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }
}