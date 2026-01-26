package ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositMoney;
import ui.pages.TransferMoney;
import ui.pages.UserDashboard;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeTransactionTest extends BaseUiTest {
    private static final Random random = new Random();

    @Test
    public void userCanMakeTransactionTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        new UserDashboard().open().createNewAccount();
        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + accounts.getFirst().getAccountNumber());
        String firstAccountNumber = accounts.getFirst().getAccountNumber();
        long firstAccountId = accounts.getFirst().getId();

        new UserDashboard().createNewAccount();
        accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        CreateAccountResponse secondAccount = accounts.stream()
                .max((a1, a2) -> Long.compare(a1.getId(), a2.getId()))
                .orElseThrow(() -> new RuntimeException("No accounts found"));
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + secondAccount.getAccountNumber());
        String secondAccountNumber = secondAccount.getAccountNumber();
        long secondAccountId = secondAccount.getId();

        // Делаем первый депозит на первый аккаунт через UI
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        int firstAccountIndex = 1;
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAccountNumber().equals(firstAccountNumber)) {
                firstAccountIndex = i + 1;
                break;
            }
        }
        depositPage.chooseAccount().selectAccount(firstAccountIndex);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        String firstDepositAmount = RandomData.getDepositAmount();
        depositPage.enterAmount(firstDepositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + firstDepositAmount + " to account " + accountNumber + "!");

        // Делаем второй депозит на первый аккаунт через UI
        depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(firstAccountIndex);
        String secondDepositAmount = RandomData.getDepositAmount();
        depositPage.enterAmount(secondDepositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + secondDepositAmount + " to account " + accountNumber + "!");

        double totalDepositAmount = Double.parseDouble(firstDepositAmount) + Double.parseDouble(secondDepositAmount);
        String transactionAmount = String.format(Locale.US, "%.2f", totalDepositAmount);

        TransferMoney transferPage = new UserDashboard().makeTransaction().getPage(TransferMoney.class);
        String recipientName = RandomData.getRecipientName();
        transferPage.chooseAccount().selectAccountByText(firstAccountNumber)
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transactionAmount)
                .clickCheckbox()
                .makeTransfer();

        transferPage.checkAlertMessageAndAccept(
                BankAlert.USER_TRANSFERRED_SUCCESSFULLY.getMessage() + transactionAmount + " to account " + secondAccountNumber + "!");

        List<CreateAccountResponse> finalAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        CreateAccountResponse firstAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + firstAccountId));
        assertThat(firstAccount.getBalance()).isZero();

        CreateAccountResponse finalSecondAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + secondAccountId));
        assertThat(finalSecondAccount.getBalance()).isEqualTo(Double.parseDouble(transactionAmount));
    }


    @Test
    public void userCanNotMakeTransactionWithInvalidAmountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        new UserDashboard().open().createNewAccount();
        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + accounts.getFirst().getAccountNumber());
        String firstAccountNumber = accounts.getFirst().getAccountNumber();
        long firstAccountId = accounts.getFirst().getId();

        new UserDashboard().createNewAccount();
        accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        CreateAccountResponse secondAccount = accounts.stream()
                .max((a1, a2) -> Long.compare(a1.getId(), a2.getId()))
                .orElseThrow(() -> new RuntimeException("No accounts found"));
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + secondAccount.getAccountNumber());
        String secondAccountNumber = secondAccount.getAccountNumber();
        long secondAccountId = secondAccount.getId();

        // Делаем первый депозит на первый аккаунт через UI
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        int firstAccountIndex = 1;
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAccountNumber().equals(firstAccountNumber)) {
                firstAccountIndex = i + 1;
                break;
            }
        }
        depositPage.chooseAccount().selectAccount(firstAccountIndex);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        String firstDepositAmount = RandomData.getDepositAmount();
        depositPage.enterAmount(firstDepositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + firstDepositAmount + " to account " + accountNumber + "!");

        // Делаем второй депозит на первый аккаунт через UI
        depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(firstAccountIndex);
        String secondDepositAmount = RandomData.getDepositAmount();
        depositPage.enterAmount(secondDepositAmount).makeDeposit();
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + secondDepositAmount + " to account " + accountNumber + "!");

        double totalDepositAmount = Double.parseDouble(firstDepositAmount) + Double.parseDouble(secondDepositAmount);
        // Генерируем невалидную сумму транзакции больше 10000
        String invalidTransactionAmount = RandomData.getInvalidTransactionAmount();

        TransferMoney transferPage = new UserDashboard().makeTransaction().getPage(TransferMoney.class);
        String recipientName = RandomData.getRecipientName();
        transferPage.chooseAccount().selectAccountByText(firstAccountNumber)
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(invalidTransactionAmount)
                .clickCheckbox()
                .makeTransfer();

        transferPage.checkAlertMessageAndAccept(
                BankAlert.USER_TRANSFERRED_UNSUCCESSFULLY.getMessage());

        List<CreateAccountResponse> finalAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        CreateAccountResponse firstAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + firstAccountId));
        assertThat(firstAccount.getBalance()).isEqualTo(totalDepositAmount);

        CreateAccountResponse finalSecondAccount = finalAccounts.stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + secondAccountId));
        assertThat(finalSecondAccount.getBalance()).isZero();
    }
}