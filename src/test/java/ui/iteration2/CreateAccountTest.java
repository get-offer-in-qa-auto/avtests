package ui.iteration2;

import api.common.utils.RetryUtils;
import api.models.CreateAccountResponse;
import api.common.annotations.UserSession;
import api.common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = RetryUtils.retry(
                "Получение списка аккаунтов",
                () -> SessionStorage.getSteps().getAllAccounts(),
                result -> result != null && !result.isEmpty(),
                10,
                2000L
        );

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}