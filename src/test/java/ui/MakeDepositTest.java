package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class MakeDepositTest {
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
    public void userCanMakeDepositTest() {
        // ШАГ 1: админ создает юзера
        CreateUserRequest userRequest = AdminSteps.createUser();
        // ШАГ 2: юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .extract()
                .header("Authorization");
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // ШАГ 3: юзер создает аккаунт
        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        Selenide.open("/dashboard");
        // ШАГ 4: юзер делает депозит
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $("select.account-selector").shouldBe(Condition.visible);
        $("select.account-selector").selectOption(1);
        String selectedAccountText = $("select.account-selector").getSelectedOptionText();
        Assertions.assertNotNull(selectedAccountText);
        String selectedAccountNumber = selectedAccountText.split(" ")[0];
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("5000.00");
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // ШАГ 6: Проверяем появление алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully deposited $5000.00 to account " + selectedAccountNumber + "!");

        alert.accept();

        $(Selectors.byText("User Dashboard")).shouldBe(Condition.visible);

        // ШАГ 6: Проверяем, что депозит создан на UI
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $(Selectors.byClassName("account-selector")).click();
        $$("select.account-selector option").findBy(Condition.text(selectedAccountNumber)).shouldHave(Condition.text(selectedAccountNumber + " (Balance: $5000.00)"));


        // ШАГ 7: Проверяем, что депозит сделан на API
        CreateAccountResponse[] userAccounts = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountWithDeposit = Arrays.stream(userAccounts)
                .filter(acc -> acc.getId() == accountResponse.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountResponse.getId()));

        assertThat(accountWithDeposit.getBalance()).isEqualTo(5000.00);


    }

    @Test
    public void userCanNotMakeDepositWithInvalidAmountTest() {
        // ШАГ 1: админ создает юзера
        CreateUserRequest userRequest = AdminSteps.createUser();
        // ШАГ 2: юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .extract()
                .header("Authorization");
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // ШАГ 3: юзер создает аккаунт
        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        Selenide.open("/dashboard");
        // ШАГ 4: юзер делает депозит
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $("select.account-selector").shouldBe(Condition.visible);
        $("select.account-selector").selectOption(1);
        String selectedAccountText = $("select.account-selector").getSelectedOptionText();
        Assertions.assertNotNull(selectedAccountText);
        String selectedAccountNumber = selectedAccountText.split(" ")[0];
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("5000.01");
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // ШАГ 6: Проверяем появление алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please deposit less or equal to 5000$.");

        alert.accept();

        // После ошибки остаемся на странице депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 6: Проверяем, что депозит не создан на UI
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $(Selectors.byClassName("account-selector")).click();
        $$("select.account-selector option").findBy(Condition.text(selectedAccountNumber)).shouldHave(Condition.text(selectedAccountNumber + " (Balance: $0.00)"));


        // ШАГ 7: Проверяем, что депозит не сделан на API
        CreateAccountResponse[] userAccounts = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountWithoutDeposit = Arrays.stream(userAccounts)
                .filter(acc -> acc.getId() == accountResponse.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountResponse.getId()));

        assertThat(accountWithoutDeposit.getBalance()).isZero();


    }
}