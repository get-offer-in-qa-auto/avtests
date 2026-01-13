package iteration1;

import generators.RandomData;
import helpers.AccountHelper;
import models.CreateUserRequest;
import models.MakeDepositRequest;
import models.UserRole;
import org.assertj.core.data.Offset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.MakeDepositRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class MakeDepositTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validBalanceData")
    public void userCanMakeDepositTest(double balance) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double balanceBefore = AccountHelper.getAccountBalance(accountId, userRequest.getUsername(), userRequest.getPassword());

        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new MakeDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        double balanceAfter = AccountHelper.getAccountBalance(accountId, userRequest.getUsername(), userRequest.getPassword());
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore + balance, Offset.offset(0.01));
    }

    @ParameterizedTest
    @MethodSource("invalidBalanceData")
    public void userCannotDepositInvalidBalanceTest(double balance, String errorValue) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double balanceBefore = AccountHelper.getAccountBalance(accountId, userRequest.getUsername(), userRequest.getPassword());

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new MakeDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorValue))
                .post(makeDepositRequest);

        double balanceAfter = AccountHelper.getAccountBalance(accountId, userRequest.getUsername(), userRequest.getPassword());
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, Offset.offset(0.01));
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAccount")
    public void userCannotDepositToInvalidAccountTest(int account, int balance) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double balanceBefore = AccountHelper.getAccountBalance(accountId, userRequest.getUsername(), userRequest.getPassword());

        // Пытаемся сделать депозит на чужой/несуществующий аккаунт
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(balance)
                .build();

        new MakeDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(depositRequest);

        double balanceAfter = AccountHelper.getAccountBalance(accountId, userRequest.getUsername(), userRequest.getPassword());
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, Offset.offset(0.01));
    }

    public static Stream<Arguments> invalidDepositAccount() {
        return Stream.of(
                Arguments.of(2, 1000),
                Arguments.of(4, 1000));
    }

    public static Stream<Arguments> validBalanceData() {
        return Stream.of(
                Arguments.of(4000),
                Arguments.of(4999.99),
                Arguments.of(0.01));

    }

    public static Stream<Arguments> invalidBalanceData() {
        return Stream.of(
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000"),
                Arguments.of(-0.01, "Deposit amount must be at least 0.01"),
                Arguments.of(0, "Deposit amount must be at least 0.01"));

    }
}