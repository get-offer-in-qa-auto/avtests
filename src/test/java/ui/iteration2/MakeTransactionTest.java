package ui.iteration2;

import api.models.CreateAccountResponse;
import api.common.annotations.UserSession;
import api.common.storage.SessionStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.DepositMoney;
import ui.pages.TransferMoney;
import ui.pages.UserDashboard;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeTransactionTest extends BaseUiTest {
    @ParameterizedTest
    @MethodSource("validTransactionData")
    @UserSession
    public void userCanMakeTransactionTest(int accountIndex, String depositAmount, String transferAmount, String recipientName) {
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

        // Делаем первый депозит на первый аккаунт через UI
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(accountIndex);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        depositPage.enterAmount(depositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + depositAmount + " to account " + accountNumber + "!");

        // Делаем второй депозит на первый аккаунт через UI
        depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccountByText(accountNumber);
        depositPage.enterAmount(depositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + depositAmount + " to account " + accountNumber + "!");

        TransferMoney transferPage = new UserDashboard().makeTransaction().getPage(TransferMoney.class);
        transferPage.chooseAccount().selectAccountByText(firstAccountNumber)
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferAmount)
                .clickCheckbox()
                .makeTransfer();

        transferPage.checkAlertMessageAndAccept(
                BankAlert.USER_TRANSFERRED_SUCCESSFULLY.getMessage() + transferAmount + " to account " + secondAccountNumber + "!");

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
        assertThat(finalSecondAccount.getBalance()).isEqualTo(Double.parseDouble(transferAmount));
    }


    @ParameterizedTest
    @MethodSource("invalidTransactionData")
    @UserSession
    public void userCanNotMakeTransactionWithInvalidAmountTest(int accountIndex, String depositAmount, String transferAmount, String recipientName) {
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

        // Делаем первый депозит на первый аккаунт через UI
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccountByText(firstAccountNumber);

        depositPage.enterAmount(depositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + depositAmount + " to account " + firstAccountNumber + "!");

        // Делаем второй депозит на первый аккаунт через UI
        depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccountByText(firstAccountNumber);
        depositPage.enterAmount(depositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + depositAmount + " to account " + firstAccountNumber + "!");

        TransferMoney transferPage = new UserDashboard().makeTransaction().getPage(TransferMoney.class);
        transferPage.chooseAccount().selectAccountByText(firstAccountNumber)
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferAmount)
                .clickCheckbox()
                .makeTransfer();

        transferPage.checkAlertMessageAndAccept(
                BankAlert.USER_TRANSFERRED_UNSUCCESSFULLY.getMessage());

        List<CreateAccountResponse> finalAccounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse firstAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + firstAccountId));
        assertThat(firstAccount.getBalance()).isEqualTo(Double.parseDouble(depositAmount) * 2);

        CreateAccountResponse finalSecondAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + secondAccountId));
        assertThat(finalSecondAccount.getBalance()).isZero();
    }

    public static Stream<Arguments> validTransactionData() {
        return Stream.of(
                Arguments.of(1, "5000.00", "10000.00", "Noname")
        );
    }

    public static Stream<Arguments> invalidTransactionData() {
        return Stream.of(
                Arguments.of(1, "5000.00", "10000.01", "Noname")
        );
    }
}