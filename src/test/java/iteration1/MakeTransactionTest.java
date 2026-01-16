package iteration1;

import helpers.AccountHelper;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
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

public class MakeTransactionTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validTransactionAmount")
    public void userCanMakeTransactionTest(double amount) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        int firstAccountId = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int secondAccountId = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double depositAmount = Math.min(amount + 100.0, 5000.0);
        if (amount > 5000.0) {
            double remainingNeeded = amount + 100.0;
            while (remainingNeeded > 0) {
                double currentDeposit = Math.min(remainingNeeded, 5000.0);
                MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                        .id(firstAccountId)
                        .balance(currentDeposit)
                        .build();
                new ValidatedCrudRequester<MakeDepositResponse>(
                        RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.DEPOSIT,
                        ResponseSpecs.requestReturnsOK())
                        .post(depositRequest);
                remainingNeeded -= currentDeposit;
            }
        } else {
            MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                    .id(firstAccountId)
                    .balance(depositAmount)
                    .build();
            new ValidatedCrudRequester<MakeDepositResponse>(
                    RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnsOK())
                    .post(depositRequest);
        }

        MakeDepositResponse senderAccountBefore = AccountHelper.getAccountById(firstAccountId, userRequest.getUsername(), userRequest.getPassword());
        MakeDepositResponse receiverAccountBefore = AccountHelper.getAccountById(secondAccountId, userRequest.getUsername(), userRequest.getPassword());

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        new ValidatedCrudRequester<MakeTransactionResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transactionRequest);

        MakeDepositResponse senderAccountAfter = AccountHelper.getAccountById(firstAccountId, userRequest.getUsername(), userRequest.getPassword());
        MakeDepositResponse receiverAccountAfter = AccountHelper.getAccountById(secondAccountId, userRequest.getUsername(), userRequest.getPassword());

        MakeDepositResponse expectedSenderAccount = MakeDepositResponse.builder()
                .id(firstAccountId)
                .balance(senderAccountBefore.getBalance() - amount)
                .build();

        MakeDepositResponse expectedReceiverAccount = MakeDepositResponse.builder()
                .id(secondAccountId)
                .balance(receiverAccountBefore.getBalance() + amount)
                .build();

        ModelAssertions.assertThatModels(expectedSenderAccount, senderAccountAfter).match();
        ModelAssertions.assertThatModels(expectedReceiverAccount, receiverAccountAfter).match();
    }

    public static Stream<Arguments> validTransactionAmount() {
        return Stream.of(
                Arguments.of(500),
                Arguments.of(9999.99),
                Arguments.of(0.01));

    }

    @ParameterizedTest
    @MethodSource("invalidAmountTransfer")
    public void userCannotTransferInvalidAmountTest(double amount, String errorType) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        int firstAccountId = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int secondAccountId = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double depositAmount = 1000.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(firstAccountId)
                .balance(depositAmount)
                .build();

        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        MakeDepositResponse senderAccountBefore = AccountHelper.getAccountById(firstAccountId, userRequest.getUsername(), userRequest.getPassword());
        MakeDepositResponse receiverAccountBefore = AccountHelper.getAccountById(secondAccountId, userRequest.getUsername(), userRequest.getPassword());

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest("message", errorType))
                .post(transactionRequest);

        MakeDepositResponse senderAccountAfter = AccountHelper.getAccountById(firstAccountId, userRequest.getUsername(), userRequest.getPassword());
        MakeDepositResponse receiverAccountAfter = AccountHelper.getAccountById(secondAccountId, userRequest.getUsername(), userRequest.getPassword());

        ModelAssertions.assertThatModels(senderAccountBefore, senderAccountAfter).match();
        ModelAssertions.assertThatModels(receiverAccountBefore, receiverAccountAfter).match();
    }

    public static Stream<Arguments> invalidAmountTransfer() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(0, "Transfer amount must be at least 0.01"),
                Arguments.of(-0.01, "Transfer amount must be at least 0.01"));
    }

    @Test
    public void userCanTransferToDifferentAccountTest() {
        CreateUserRequest senderUserRequest = AdminSteps.createUser();
        CreateUserRequest receiverUserRequest = AdminSteps.createUser();

        int senderAccountId = new CrudRequester(RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int receiverAccountId = new CrudRequester(RequestSpecs.authAsUser(receiverUserRequest.getUsername(), receiverUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double transferAmount = 500.0;
        double depositAmount = transferAmount + 100.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        MakeDepositResponse senderAccountBefore = AccountHelper.getAccountById(senderAccountId, senderUserRequest.getUsername(), senderUserRequest.getPassword());
        MakeDepositResponse receiverAccountBefore = AccountHelper.getAccountById(receiverAccountId, receiverUserRequest.getUsername(), receiverUserRequest.getPassword());

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new ValidatedCrudRequester<MakeTransactionResponse>(
                RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transactionRequest);

        MakeDepositResponse senderAccountAfter = AccountHelper.getAccountById(senderAccountId, senderUserRequest.getUsername(), senderUserRequest.getPassword());
        MakeDepositResponse receiverAccountAfter = AccountHelper.getAccountById(receiverAccountId, receiverUserRequest.getUsername(), receiverUserRequest.getPassword());

        MakeDepositResponse expectedSenderAccount = MakeDepositResponse.builder()
                .id(senderAccountId)
                .balance(senderAccountBefore.getBalance() - transferAmount)
                .build();

        MakeDepositResponse expectedReceiverAccount = MakeDepositResponse.builder()
                .id(receiverAccountId)
                .balance(receiverAccountBefore.getBalance() + transferAmount)
                .build();

        ModelAssertions.assertThatModels(expectedSenderAccount, senderAccountAfter).match();
        ModelAssertions.assertThatModels(expectedReceiverAccount, receiverAccountAfter).match();
    }

    @Test
    public void userCannotTransferMoreThanAccountBalanceTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        int firstAccountId = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int secondAccountId = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double depositAmount = 500.0;
        double transferAmount = depositAmount + 100.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(firstAccountId)
                .balance(depositAmount)
                .build();

        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        MakeDepositResponse senderAccountBefore = AccountHelper.getAccountById(firstAccountId, userRequest.getUsername(), userRequest.getPassword());
        MakeDepositResponse receiverAccountBefore = AccountHelper.getAccountById(secondAccountId, userRequest.getUsername(), userRequest.getPassword());

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(transferAmount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest("message", "Invalid transfer: insufficient funds or invalid accounts"))
                .post(transactionRequest);

        MakeDepositResponse senderAccountAfter = AccountHelper.getAccountById(firstAccountId, userRequest.getUsername(), userRequest.getPassword());
        MakeDepositResponse receiverAccountAfter = AccountHelper.getAccountById(secondAccountId, userRequest.getUsername(), userRequest.getPassword());

        ModelAssertions.assertThatModels(senderAccountBefore, senderAccountAfter).match();
        ModelAssertions.assertThatModels(receiverAccountBefore, receiverAccountAfter).match();
    }
}