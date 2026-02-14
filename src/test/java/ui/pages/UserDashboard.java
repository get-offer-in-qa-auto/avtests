package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement makeDeposit = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement makeTransaction = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement changeName = $(Selectors.byClassName("user-info"));



    @Override
    public String url() {
        return "/dashboard";
    }

    @Step("Создать новый аккаунт")
    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    @Step("Перейти к депозиту")
    public UserDashboard makeDeposit() {
        makeDeposit.click();
        return this;
    }

    @Step("Перейти к переводу")
    public UserDashboard makeTransaction() {
        makeTransaction.click();
        return this;
    }

    @Step("Открыть панель редактирования имени")
    public UserDashboard changeName() {
        changeName.shouldBe(Condition.visible, Duration.ofSeconds(15));
        changeName.click();
        return this;
    }

}
