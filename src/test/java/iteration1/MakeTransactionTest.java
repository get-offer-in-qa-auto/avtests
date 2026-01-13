package iteration1;

import generators.RandomData;
import helpers.AccountHelper;
import models.CreateUserRequest;
import models.MakeDepositRequest;
import models.MakeTransactionRequest;
import models.UserRole;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.MakeDepositRequester;
import requests.MakeTransactionRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class MakeTransactionTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validTransactionAmount")
    public void userCanMakeTransactionTest(double amount) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int receiverAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double depositAmount = Math.min(amount + 100.0, 5000.0);
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new MakeDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new MakeTransactionRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transactionRequest);

        double senderBalanceAfter = AccountHelper.getAccountBalance(senderAccountId, userRequest.getUsername(), userRequest.getPassword());
        double receiverBalanceAfter = AccountHelper.getAccountBalance(receiverAccountId, userRequest.getUsername(), userRequest.getPassword());

        softly.assertThat(senderBalanceAfter).isCloseTo(depositAmount - amount, Offset.offset(0.01));
        softly.assertThat(receiverBalanceAfter).isCloseTo(amount, Offset.offset(0.01));
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
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int receiverAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double depositAmount = 1000.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new MakeDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new MakeTransactionRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorType))
                .post(transactionRequest);

        double senderBalanceAfter = AccountHelper.getAccountBalance(senderAccountId, userRequest.getUsername(), userRequest.getPassword());
        double receiverBalanceAfter = AccountHelper.getAccountBalance(receiverAccountId, userRequest.getUsername(), userRequest.getPassword());

        softly.assertThat(senderBalanceAfter).isCloseTo(depositAmount, Offset.offset(0.01));
        softly.assertThat(receiverBalanceAfter).isCloseTo(0.0, Offset.offset(0.01));
    }

    public static Stream<Arguments> invalidAmountTransfer() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(0, "Transfer amount must be at least 0.01"),
                Arguments.of(-0.01, "Transfer amount must be at least 0.01"));
    }

    @Test
    public void userCanTransferToDifferentAccountTest() {
        CreateUserRequest senderUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(senderUserRequest);

        CreateUserRequest receiverUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(receiverUserRequest);

        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int receiverAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(receiverUserRequest.getUsername(), receiverUserRequest.getPassword()),
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

        new MakeDepositRequester(RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new MakeTransactionRequester(RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transactionRequest);

        double senderBalanceAfter = AccountHelper.getAccountBalance(senderAccountId, senderUserRequest.getUsername(), senderUserRequest.getPassword());
        double receiverBalanceAfter = AccountHelper.getAccountBalance(receiverAccountId, receiverUserRequest.getUsername(), receiverUserRequest.getPassword());

        softly.assertThat(senderBalanceAfter).isCloseTo(depositAmount - transferAmount, Offset.offset(0.01));
        softly.assertThat(receiverBalanceAfter).isCloseTo(transferAmount, Offset.offset(0.01));
    }

    @Test
    public void userCannotTransferMoreThanAccountBalanceTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int receiverAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        double depositAmount = 500.0;
        double transferAmount = depositAmount + 100.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new MakeDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new MakeTransactionRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithMessage("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transactionRequest);

        double senderBalanceAfter = AccountHelper.getAccountBalance(senderAccountId, userRequest.getUsername(), userRequest.getPassword());
        double receiverBalanceAfter = AccountHelper.getAccountBalance(receiverAccountId, userRequest.getUsername(), userRequest.getPassword());

        softly.assertThat(senderBalanceAfter).isCloseTo(depositAmount, Offset.offset(0.01));
        softly.assertThat(receiverBalanceAfter).isCloseTo(0.0, Offset.offset(0.01));
    }
}