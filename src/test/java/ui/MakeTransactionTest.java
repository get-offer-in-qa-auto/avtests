package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.*;
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

public class MakeTransactionTest {
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
    public void userCanMakeTransactionTest() {
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

        // ШАГ 4: юзер создает еще один аккаунт
        CreateAccountResponse response = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // ШАГ 5: юзер делает два депозита по 5000 на первый аккаунт
        String firstAccountNumber = accountResponse.getAccountNumber();
        String secondAccountNumber = response.getAccountNumber();

        MakeDepositRequest depositRequest1 = MakeDepositRequest.builder()
                .id((int) accountResponse.getId())
                .balance(5000.00)
                .build();
        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest1);

        MakeDepositRequest depositRequest2 = MakeDepositRequest.builder()
                .id((int) accountResponse.getId())
                .balance(5000.00)
                .build();
        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest2);

        // ШАГ 6: юзер делает транзакцию с первого аккаунта на второй
        Selenide.open("/dashboard");
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").shouldBe(Condition.visible);
        $("select.account-selector").selectOptionContainingText(firstAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("Noname");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(secondAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("10000");
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: Проверяем появление алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully transferred $10000 to account " + secondAccountNumber + "!");

        alert.accept();

        // ШАГ 8: Проверяем на UI, что балансы обновились
        Selenide.refresh();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").shouldBe(Condition.visible);
        $$("select.account-selector option").findBy(Condition.text(firstAccountNumber)).shouldHave(Condition.text(firstAccountNumber + " (Balance: $0.00)"));
        $$("select.account-selector option").findBy(Condition.text(secondAccountNumber)).shouldHave(Condition.text(secondAccountNumber + " (Balance: $10000.00)"));

        // ШАГ 9: Проверяем через API, что транзакция выполнена корректно
        CreateAccountResponse[] userAccounts = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse firstAccount = Arrays.stream(userAccounts)
                .filter(acc -> acc.getId() == accountResponse.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountResponse.getId()));
        assertThat(firstAccount.getBalance()).isZero();

        CreateAccountResponse secondAccount = Arrays.stream(userAccounts)
                .filter(acc -> acc.getId() == response.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + response.getId()));
        assertThat(secondAccount.getBalance()).isEqualTo(10000.00);


    }


    @Test
    public void userCanNotMakeTransactionWithInvalidAmountTest() {
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

        // ШАГ 4: юзер создает еще один аккаунт
        CreateAccountResponse response = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // ШАГ 5: юзер делает два депозита по 5000 на первый аккаунт
        String firstAccountNumber = accountResponse.getAccountNumber();
        String secondAccountNumber = response.getAccountNumber();

        MakeDepositRequest depositRequest1 = MakeDepositRequest.builder()
                .id((int) accountResponse.getId())
                .balance(5000.00)
                .build();
        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest1);

        MakeDepositRequest depositRequest2 = MakeDepositRequest.builder()
                .id((int) accountResponse.getId())
                .balance(5000.00)
                .build();
        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest2);

        // ШАГ 6: юзер делает транзакцию с первого аккаунта на второй
        Selenide.open("/dashboard");
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").shouldBe(Condition.visible);
        $("select.account-selector").selectOptionContainingText(firstAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("Noname");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(secondAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("10000.01");
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: Проверяем появление алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Error: Transfer amount cannot exceed 10000");

        alert.accept();

        // ШАГ 8: Проверяем на UI, что балансы не обновились обновились
        Selenide.refresh();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").shouldBe(Condition.visible);
        $$("select.account-selector option").findBy(Condition.text(firstAccountNumber)).shouldHave(Condition.text(firstAccountNumber + " (Balance: $10000.00)"));
        $$("select.account-selector option").findBy(Condition.text(secondAccountNumber)).shouldHave(Condition.text(secondAccountNumber + " (Balance: $0.00)"));

        // ШАГ 9: Проверяем через API, что транзакция не была выполнена и балансы не изменились
        CreateAccountResponse[] userAccounts = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse firstAccount = Arrays.stream(userAccounts)
                .filter(acc -> acc.getId() == accountResponse.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountResponse.getId()));
        assertThat(firstAccount.getBalance()).isEqualTo(10000.00);

        CreateAccountResponse secondAccount = Arrays.stream(userAccounts)
                .filter(acc -> acc.getId() == response.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + response.getId()));
        assertThat(secondAccount.getBalance()).isZero();


    }

}
