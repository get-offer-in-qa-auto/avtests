package ui.pages;

import api.common.utils.RetryUtils;
import com.codeborne.selenide.Selectors;
import io.qameta.allure.Step;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class TransferMoney extends BasePage<TransferMoney> {
    private final SelenideElement welcomeText = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private final SelenideElement accountSelector = $("select.account-selector");
    private final SelenideElement enterRecipientName = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private final SelenideElement enterRecipientAccountNumber = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private final SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement checkboxClick = $(Selectors.byId("confirmCheck"));
    private final SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    @Step("Выбрать аккаунт отправителя")
    public TransferMoney chooseAccount() {
        accountSelector.click();
        return this;
    }

    @Step("Выбрать аккаунт по индексу {index}")
    public TransferMoney selectAccount(int index) {
        accountSelector.selectOption(index);
        return this;
    }

    @Step("Выбрать аккаунт {accountNumber}")
    public TransferMoney selectAccountByText(String accountNumber) {
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

    @Step("Ввести имя получателя {name}")
    public TransferMoney enterRecipientName(String name) {
        enterRecipientName.sendKeys(name);
        return this;
    }

    @Step("Ввести номер аккаунта получателя {accountNumber}")
    public TransferMoney enterRecipientAccountNumber(String accountNumber) {
        enterRecipientAccountNumber.sendKeys(accountNumber);
        return this;
    }

    @Step("Ввести сумму перевода {amount}")
    public TransferMoney enterAmount(String amount) {
        enterAmount.sendKeys(amount);
        return this;
    }

    @Step("Подтвердить перевод")
    public TransferMoney clickCheckbox() {
        checkboxClick.click();
        return this;
    }

    @Step("Выполнить перевод")
    public TransferMoney makeTransfer() {
        sendTransferButton.click();
        return this;
    }
}
