package ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositMoney;
import ui.pages.UserDashboard;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeDepositTest extends BaseUiTest {
    private static final Random random = new Random();

    @Test
    public void userCanMakeDepositTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        
        new UserDashboard().open().createNewAccount();
        
        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());
        
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        
        // Выбираем рандомный аккаунт
        int randomAccountIndex = random.nextInt(createdAccounts.size()) + 1;
        depositPage.chooseAccount().selectAccount(randomAccountIndex);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];
        
        // Генерируем рандомную сумму депозита
        String depositAmount = RandomData.getDepositAmount();
        depositPage.enterAmount(depositAmount).makeDeposit();
        
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + depositAmount + " to account " + accountNumber + "!");

        // Проверяем, что депозит сделан на API
        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        CreateAccountResponse accountWithDeposit = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        assertThat(accountWithDeposit.getBalance()).isEqualTo(Double.parseDouble(depositAmount));
    }

    @Test
    public void userCanNotMakeDepositWithInvalidAmountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        
        int randomAccountIndex = random.nextInt(createdAccounts.size()) + 1;
        depositPage.chooseAccount().selectAccount(randomAccountIndex);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        String invalidDepositAmount = RandomData.getInvalidDepositAmount();
        depositPage.enterAmount(invalidDepositAmount).makeDeposit();

        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_UNSUCCESSFULLY.getMessage());

        // Проверяем, что депозит не был сделан на API
        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
        CreateAccountResponse accountWithoutDeposit = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        assertThat(accountWithoutDeposit.getBalance()).isZero();
    }
}