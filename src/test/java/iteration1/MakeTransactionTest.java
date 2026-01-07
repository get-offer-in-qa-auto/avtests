package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.given;

public class MakeTransactionTest {
    @BeforeAll
    public static void setupRestAssured(){
        RestAssured.filters(
                List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }
    @ParameterizedTest
    @MethodSource("validTransactionAmount")
    public void userCanTransferAmount(double amount){
        String requestBody = String.format(Locale.ROOT, """
                {
                "senderAccountId": 1,
                "receiverAccountId": 2,
                "amount": %.2f
                }
                """, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
    public static Stream<Arguments> validTransactionAmount(){
        return Stream.of(
                Arguments.of(500),
                Arguments.of(9999.99),
                Arguments.of(0.01));

    }
    @ParameterizedTest
    @MethodSource("invalidAmountTransfer")
    public void userCannotTransferInvalidAmount(double amount, String errorType){
        String requestBody = String.format(Locale.ROOT, """
               {
                "senderAccountId": 1,
                "receiverAccountId": 2,
                "amount": %.2f
                }
               """, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorType));
    }
    public static Stream<Arguments> invalidAmountTransfer(){
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(0, "Transfer amount must be at least 0.01"),
                Arguments.of(-0.01, "Transfer amount must be at least 0.01"));
    }

    @ParameterizedTest
    @MethodSource("transferToValidAccount")
    public void userCanTransferToDifferentAccount(int account){
        String requestBody = String.format(Locale.ROOT, """
               {
                "senderAccountId": 1,
                "receiverAccountId": %d,
                "amount": 500
                }
               """, account);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization","Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
    public static Stream<Arguments> transferToValidAccount(){
        return Stream.of(
                Arguments.of(2),
                Arguments.of(3));
    }

    @Test
    public void userCannotTransferMoreThanAccountBalance(){
        String accountsResponse =
                given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();

        JsonPath jsonPath = new JsonPath(accountsResponse);
        List<Map<String, Object>> accounts = jsonPath.getList("$");
        
        double accountBalance = 0.0;
        for (Map<String, Object> account : accounts) {
            Object id = account.get("id");
            if (id != null) {
                int accountId = id instanceof Number ? ((Number) id).intValue() : Integer.parseInt(id.toString());
                if (accountId == 1) {
                    Object balance = account.get("balance");
                    if (balance instanceof Number) {
                        accountBalance = ((Number) balance).doubleValue();
                    }
                    break;
                }
            }
        }

        // Пытаемся перевести сумму больше баланса
        double transferAmount = accountBalance + 100.0;
        String requestBody = String.format(Locale.ROOT, """
               {
                "senderAccountId": 1,
                "receiverAccountId": 2,
                "amount": %.2f
                }
               """, transferAmount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }
}
