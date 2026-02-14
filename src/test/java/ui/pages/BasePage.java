package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.ElementsCollection;
import io.qameta.allure.Step;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import ui.elements.BaseElement;

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
        String captured = String.valueOf(executeJavaScript("return window." + ALERT_CAPTURE_KEY + " || '';"));
        assertThat(captured).contains(bankAlert);
        executeJavaScript("window." + ALERT_CAPTURE_KEY + " = null;");
        return (T) this;
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