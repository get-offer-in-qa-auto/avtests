package ui.iteration2;

import api.models.CreateAccountResponse;
import api.common.annotations.UserSession;
import api.common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositMoney;
import ui.pages.TransferMoney;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeTransactionTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanMakeTransactionTest() {
        new UserDashboard().open().createNewAccount();
        List<CreateAccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + accounts.getFirst().getAccountNumber());
        String firstAccountNumber = accounts.getFirst().getAccountNumber();
        long firstAccountId = accounts.getFirst().getId();

        new UserDashboard().createNewAccount();
        accounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse secondAccount = accounts.stream()
                .max((a1, a2) -> Long.compare(a1.getId(), a2.getId()))
                .orElseThrow(() -> new RuntimeException("No accounts found"));
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + secondAccount.getAccountNumber());
        String secondAccountNumber = secondAccount.getAccountNumber();
        long secondAccountId = secondAccount.getId();

        // Делаем первый депозит по 5000 на первый аккаунт через UI
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(1);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        depositPage.enterAmount("5000.00").makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + "5000.00 to account " + accountNumber + "!");

        // Делаем второй депозит по 5000 на первый аккаунт через UI
        depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(1);
        depositPage.enterAmount("5000.00").makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + "5000.00 to account " + accountNumber + "!");

        TransferMoney transferPage = new UserDashboard().makeTransaction().getPage(TransferMoney.class);
        transferPage.chooseAccount().selectAccountByText(firstAccountNumber)
                .enterRecipientName("Noname")
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount("10000.00")
                .clickCheckbox()
                .makeTransfer();

        transferPage.checkAlertMessageAndAccept(
                BankAlert.USER_TRANSFERRED_SUCCESSFULLY.getMessage() + "10000.00 to account " + secondAccountNumber + "!");

        List<CreateAccountResponse> finalAccounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse firstAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + firstAccountId));
        assertThat(firstAccount.getBalance()).isZero();

        CreateAccountResponse finalSecondAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + secondAccountId));
        assertThat(finalSecondAccount.getBalance()).isEqualTo(10000.00);
    }


    @Test
    @UserSession
    public void userCanNotMakeTransactionWithInvalidAmountTest() {
        new UserDashboard().open().createNewAccount();
        List<CreateAccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + accounts.getFirst().getAccountNumber());
        String firstAccountNumber = accounts.getFirst().getAccountNumber();
        long firstAccountId = accounts.getFirst().getId();

        new UserDashboard().createNewAccount();
        accounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse secondAccount = accounts.stream()
                .max((a1, a2) -> Long.compare(a1.getId(), a2.getId()))
                .orElseThrow(() -> new RuntimeException("No accounts found"));
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + secondAccount.getAccountNumber());
        String secondAccountNumber = secondAccount.getAccountNumber();
        long secondAccountId = secondAccount.getId();

        // Делаем первый депозит по 5000 на первый аккаунт через UI
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(1);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        depositPage.enterAmount("5000.00").makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + "5000.00 to account " + accountNumber + "!");

        // Делаем второй депозит по 5000 на первый аккаунт через UI
        depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(1);
        depositPage.enterAmount("5000.00").makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + "5000.00 to account " + accountNumber + "!");

        TransferMoney transferPage = new UserDashboard().makeTransaction().getPage(TransferMoney.class);
        transferPage.chooseAccount().selectAccountByText(firstAccountNumber)
                .enterRecipientName("Noname")
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount("10000.01")
                .clickCheckbox()
                .makeTransfer();

        transferPage.checkAlertMessageAndAccept(
                BankAlert.USER_TRANSFERRED_UNSUCCESSFULLY.getMessage());

        List<CreateAccountResponse> finalAccounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse firstAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + firstAccountId));
        assertThat(firstAccount.getBalance()).isEqualTo(10000.00);

        CreateAccountResponse finalSecondAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + secondAccountId));
        assertThat(finalSecondAccount.getBalance()).isZero();
    }
}