package iteration1;

import helpers.AccountHelper;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.MakeDepositRequest;
import models.MakeDepositResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class MakeDepositTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validBalanceData")
    public void userCanMakeDepositTest(double balance) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int accountId = (int) accountResponse.getId();

        MakeDepositResponse accountBefore = AccountHelper.getAccountById(accountId, userRequest.getUsername(), userRequest.getPassword());

        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        MakeDepositResponse depositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        ModelAssertions.assertThatModels(depositRequest, depositResponse).match();

        MakeDepositResponse accountAfter = AccountHelper.getAccountById(accountId, userRequest.getUsername(), userRequest.getPassword());

        MakeDepositResponse expectedAccount = MakeDepositResponse.builder()
                .id(accountId)
                .balance(accountBefore.getBalance() + balance)
                .build();

        ModelAssertions.assertThatModels(expectedAccount, accountAfter).match();
    }

    @ParameterizedTest
    @MethodSource("invalidBalanceData")
    public void userCannotDepositInvalidBalanceTest(double balance, String errorValue) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int accountId = (int) accountResponse.getId();

        MakeDepositResponse accountBefore = AccountHelper.getAccountById(accountId, userRequest.getUsername(), userRequest.getPassword());

        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorValue))
                .post(depositRequest);

        MakeDepositResponse accountAfter = AccountHelper.getAccountById(accountId, userRequest.getUsername(), userRequest.getPassword());

        ModelAssertions.assertThatModels(accountBefore, accountAfter).match();
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAccount")
    public void userCannotDepositToInvalidAccountTest(int account, int balance) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int accountId = (int) accountResponse.getId();

        MakeDepositResponse accountBefore = AccountHelper.getAccountById(accountId, userRequest.getUsername(), userRequest.getPassword());

        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(balance)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden())
                .post(depositRequest);

        MakeDepositResponse accountAfter = AccountHelper.getAccountById(accountId, userRequest.getUsername(), userRequest.getPassword());

        ModelAssertions.assertThatModels(accountBefore, accountAfter).match();
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