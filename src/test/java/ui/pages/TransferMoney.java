package ui.pages;

import com.codeborne.selenide.Selectors;
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

    public TransferMoney chooseAccount() {
        accountSelector.click();
        return this;
    }

    public TransferMoney selectAccount(int index) {
        accountSelector.selectOption(index);
        return this;
    }

    public TransferMoney selectAccountByText(String accountNumber) {
        accountSelector.selectOptionContainingText(accountNumber);
        return this;
    }

    public TransferMoney enterRecipientName(String name) {
        enterRecipientName.sendKeys(name);
        return this;
    }

    public TransferMoney enterRecipientAccountNumber(String accountNumber) {
        enterRecipientAccountNumber.sendKeys(accountNumber);
        return this;
    }

    public TransferMoney enterAmount(String amount) {
        enterAmount.sendKeys(amount);
        return this;
    }

    public TransferMoney clickCheckbox() {
        checkboxClick.click();
        return this;
    }

    public TransferMoney makeTransfer() {
        sendTransferButton.click();
        return this;
    }

    public String getSuccessTransferMessage(String amount, String accountNumber) {
        return BankAlert.USER_TRANSFERRED_SUCCESSFULLY.getMessage() + amount + " to account " + accountNumber + "!";
    }
}
