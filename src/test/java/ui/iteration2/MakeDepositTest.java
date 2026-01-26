package ui.iteration2;

import api.models.CreateAccountResponse;
import api.common.annotations.UserSession;
import api.common.storage.SessionStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.DepositMoney;
import ui.pages.UserDashboard;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeDepositTest extends BaseUiTest {
    @ParameterizedTest
    @MethodSource("validDepositData")
    @UserSession
    public void userCanMakeDepositTest(int accountIndex, String amount) {
        new UserDashboard().open().createNewAccount();
        
        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();
        
        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());
        
        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(accountIndex);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];
        
        depositPage.enterAmount(amount).makeDeposit();
        
        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_SUCCESSFULLY.getMessage() + amount + " to account " + accountNumber + "!");

        // Проверяем, что депозит сделан на API
        List<CreateAccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse accountWithDeposit = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        assertThat(accountWithDeposit.getBalance()).isEqualTo(Double.parseDouble(amount));
    }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    @UserSession
    public void userCanNotMakeDepositWithInvalidAmountTest(int accountIndex, String amount) {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        DepositMoney depositPage = new UserDashboard().makeDeposit().getPage(DepositMoney.class);
        depositPage.chooseAccount().selectAccount(accountIndex);
        String accountNumber = depositPage.getAccountSelector().getSelectedOptionText().split(" ")[0];

        depositPage.enterAmount(amount).makeDeposit();

        depositPage.checkAlertMessageAndAccept(
                BankAlert.USER_DEPOSITED_UNSUCCESSFULLY.getMessage());

        // Проверяем, что депозит не был сделан на API
        List<CreateAccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        CreateAccountResponse accountWithoutDeposit = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        assertThat(accountWithoutDeposit.getBalance()).isZero();
    }

    public static Stream<Arguments> validDepositData() {
        return Stream.of(
                Arguments.of(1, "5000.00")
        );
    }

    public static Stream<Arguments> invalidDepositData() {
        return Stream.of(
                Arguments.of(1, "5000.01")
        );
    }
}