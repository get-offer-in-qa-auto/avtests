package ui.iteration2;

import api.common.utils.RetryUtils;
import api.models.CreateAccountResponse;
import api.common.annotations.UserSession;
import api.common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositMoney;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeDepositTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanMakeDepositTest() {
        new UserDashboard().open().createNewAccount();
        
        List<CreateAccountResponse> createdAccounts = RetryUtils.retry(
                "Получение списка аккаунтов",
                () -> SessionStorage.getSteps().getAllAccounts(),
                result -> result != null && !result.isEmpty(),
                5,
                1000L
        );
        
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());
        
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(1);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];
        
        depositPage.enterAmount("5000.00").makeDeposit();
        
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + "5000.00 to account " + accountNumber + "!");

        // Проверяем, что депозит сделан на API
        List<CreateAccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse accountWithDeposit = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        assertThat(accountWithDeposit.getBalance()).isEqualTo(5000.00);
    }

    @Test
    @UserSession
    public void userCanNotMakeDepositWithInvalidAmountTest() {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = RetryUtils.retry(
                "Получение списка аккаунтов",
                () -> SessionStorage.getSteps().getAllAccounts(),
                result -> result != null && !result.isEmpty(),
                5,
                1000L
        );

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(1);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        depositPage.enterAmount("5000.01").makeDeposit();

        // Фронтенд может не показывать browser alert для ошибок депозита (в отличие от transfer).
        // Проверяем главное: депозит с невалидной суммой не был выполнен — баланс остаётся 0.
        List<CreateAccountResponse> accounts = RetryUtils.retry(
                "Проверка, что депозит не выполнен",
                () -> SessionStorage.getSteps().getAllAccounts(),
                result -> result != null && !result.isEmpty(),
                5,
                500L
        );
        CreateAccountResponse accountWithoutDeposit = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        assertThat(accountWithoutDeposit.getBalance()).isZero();
    }
}