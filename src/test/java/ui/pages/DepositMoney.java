package ui.pages;

import api.common.utils.RetryUtils;
import com.codeborne.selenide.Selectors;
import io.qameta.allure.Step;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositMoney extends BasePage<DepositMoney> {
    private final SelenideElement welcomeText = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private final SelenideElement accountSelector = $("select.account-selector");
    private final SelenideElement enterAmountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement buttonDeposit = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/dashboard";
    }

    @Step("Выбрать аккаунт")
    public DepositMoney chooseAccount() {
        accountSelector.click();
        return this;
    }

    @Step("Выбрать аккаунт по индексу {index}")
    public DepositMoney selectAccount(int index) {
        accountSelector.selectOption(index);
        return this;
    }

    @Step("Выбрать аккаунт {accountNumber}")
    public DepositMoney selectAccountByText(String accountNumber) {
        RetryUtils.retry(
                "Выбор аккаунта " + accountNumber,
                () -> {
                    try {
                        accountSelector.selectOptionContainingText(accountNumber);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                },
                result -> result,
                3,
                1000L
        );
        return this;
    }

    @Step("Ввести сумму {amount}")
    public DepositMoney enterAmount(String amount) {
        enterAmountInput.clear();
        enterAmountInput.sendKeys(amount);
        return this;
    }

    @Step("Выполнить депозит")
    public DepositMoney makeDeposit() {
        buttonDeposit.click();
        return this;
    }
}
