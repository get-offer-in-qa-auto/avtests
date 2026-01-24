package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositMoney extends BasePage<DepositMoney> {
    private final SelenideElement welcomeText = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private final SelenideElement accountSelector = $("select.account-selector");
    private final SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement buttonDeposit = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public DepositMoney chooseAccount() {
        accountSelector.click();
        return this;
    }

    public DepositMoney selectAccount(int index) {
        accountSelector.selectOption(index);
        return this;
    }

    public DepositMoney selectAccountByText(String accountNumber) {
        accountSelector.selectOptionContainingText(accountNumber);
        return this;
    }

    public DepositMoney enterAmount(String amount) {
        enterAmount.sendKeys(amount);
        return this;
    }

    public DepositMoney makeDeposit() {
        buttonDeposit.click();
        return this;
    }
}
