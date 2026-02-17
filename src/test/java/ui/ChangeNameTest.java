package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomModelGenerator;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest {
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

    @Test
    public void userCanChangeNameTest() {
        // ШАГ 1: админ создает юзера
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 2: юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГ 3: генерируем случайное имя
        ChangeNameRequest changeNameRequest = RandomModelGenerator.generate(ChangeNameRequest.class);
        String newName = changeNameRequest.getName();

        // ШАГ 4: юзер меняет имя
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 5: проверка отображения алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Name updated successfully!");

        alert.accept();

        // ШАГ 6: проверка, что имя было изменено на UI
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldHave(Condition.value(newName));

        // ШАГ 7: проверка, что имя было изменено на API
        ChangeNameResponse profileResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        assertThat(profileResponse.getName()).isEqualTo(newName);
    }

    @Test
    public void userCanNotLeaveNameEmptyTest() {
        // ШАГ 1: админ создает юзера
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 2: юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГ 3: получаем исходное имя пользователя
        ChangeNameResponse initialProfile = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile.getName();

        // ШАГ 4: юзер пытается изменить имя на пустое
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(" ");
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 5: проверка отображения алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        // UI может возвращать разные сообщения в зависимости от типа ошибки
        assertThat(alertText).satisfiesAnyOf(
                text -> assertThat(text).contains("Name must contain two words with letters only"),
                text -> assertThat(text).contains("Please enter a valid name")
        );

        alert.accept();

        // ШАГ 6: проверка, что имя не было изменено на UI
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible);
        if (originalName != null && !originalName.isEmpty()) {
            $(Selectors.byAttribute("placeholder", "Enter new name")).shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = $(Selectors.byAttribute("placeholder", "Enter new name")).getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // ШАГ 7: проверка, что имя не было изменено на API
        ChangeNameResponse profileResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        assertThat(profileResponse.getName()).isEqualTo(originalName);
    }

    @Test
    public void userCanNotChangeNameWithNumbers() {
        // ШАГ 1: админ создает юзера
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 2: юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГ 3: получаем исходное имя пользователя
        ChangeNameResponse initialProfile = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile.getName();

        // ШАГ 4: юзер пытается изменить имя на имя с цифрами
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys("Alex Petrov1993");
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 5: проверка отображения алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        // UI может возвращать разные сообщения в зависимости от типа ошибки
        assertThat(alertText).satisfiesAnyOf(
                text -> assertThat(text).contains("Name must contain two words with letters only"),
                text -> assertThat(text).contains("Please enter a valid name")
        );

        alert.accept();

        // ШАГ 6: проверка, что имя не было изменено на UI
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible);
        if (originalName != null && !originalName.isEmpty()) {
            $(Selectors.byAttribute("placeholder", "Enter new name")).shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = $(Selectors.byAttribute("placeholder", "Enter new name")).getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // ШАГ 7: проверка, что имя не было изменено на API
        ChangeNameResponse profileResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        assertThat(profileResponse.getName()).isEqualTo(originalName);
    }

    @Test
    public void userCanNotChangeNameWithSymbols() {
        // ШАГ 1: админ создает юзера
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 2: юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГ 3: получаем исходное имя пользователя
        ChangeNameResponse initialProfile = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile.getName();

        // ШАГ 4: юзер пытается изменить имя на имя с символами
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys("Alex Petrov$");
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 5: проверка отображения алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        // UI может возвращать разные сообщения в зависимости от типа ошибки
        assertThat(alertText).satisfiesAnyOf(
                text -> assertThat(text).contains("Name must contain two words with letters only"),
                text -> assertThat(text).contains("Please enter a valid name")
        );

        alert.accept();

        // ШАГ 6: проверка, что имя не было изменено на UI
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible);
        if (originalName != null && !originalName.isEmpty()) {
            $(Selectors.byAttribute("placeholder", "Enter new name")).shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = $(Selectors.byAttribute("placeholder", "Enter new name")).getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // ШАГ 7: проверка, что имя не было изменено на API
        ChangeNameResponse profileResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        assertThat(profileResponse.getName()).isEqualTo(originalName);
    }

    @Test
    public void userCanNotChangeNameWithSpace() {
        // ШАГ 1: админ создает юзера
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 2: юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГ 3: получаем исходное имя пользователя
        ChangeNameResponse initialProfile = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile.getName();

        // ШАГ 4: юзер пытается изменить имя на имя без пробела
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys("AlexPetrov");
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 5: проверка отображения алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        // UI может возвращать разные сообщения в зависимости от типа ошибки
        assertThat(alertText).satisfiesAnyOf(
                text -> assertThat(text).contains("Name must contain two words with letters only"),
                text -> assertThat(text).contains("Please enter a valid name")
        );

        alert.accept();

        // ШАГ 6: проверка, что имя не было изменено на UI
        Selenide.open("/edit-profile");
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible);
        if (originalName != null && !originalName.isEmpty()) {
            $(Selectors.byAttribute("placeholder", "Enter new name")).shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = $(Selectors.byAttribute("placeholder", "Enter new name")).getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // ШАГ 7: проверка, что имя не было изменено на API
        ChangeNameResponse profileResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        assertThat(profileResponse.getName()).isEqualTo(originalName);
    }
}