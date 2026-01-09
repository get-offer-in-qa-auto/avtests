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
import static org.junit.jupiter.api.Assertions.*;

import static io.restassured.RestAssured.given;

public class MakeTransactionTest {
    @BeforeAll
    public static void setupRestAssured(){
        RestAssured.filters(
                List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }
    
    private double getAccountBalance(int accountId) {
        List<Map<String, Object>> accounts = getAllAccounts();
        return accounts.stream()
                .filter(acc -> {
                    Object id = acc.get("id");
                    return id != null && ((Number) id).intValue() == accountId;
                })
                .findFirst()
                .map(acc -> ((Number) acc.get("balance")).doubleValue())
                .orElse(0.0);
    }
    
    private List<Map<String, Object>> getAllAccounts() {
        String response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .extract()
                .asString();
        return new JsonPath(response).getList("$");
    }
    @ParameterizedTest
    @MethodSource("validTransactionAmount")
    public void userCanTransferAmount(double amount){
        double senderBalanceBefore = getAccountBalance(1);
        double receiverBalanceBefore = getAccountBalance(2);
        
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(String.format(Locale.ROOT, """
                        {
                        "senderAccountId": 1,
                        "receiverAccountId": 2,
                        "amount": %.2f
                        }
                        """, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        
        assertEquals(senderBalanceBefore - amount, getAccountBalance(1), 0.01, 
            "Sender balance should decrease");
        assertEquals(receiverBalanceBefore + amount, getAccountBalance(2), 0.01, 
            "Receiver balance should increase");
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
        double senderBalanceBefore = getAccountBalance(1);
        double receiverBalanceBefore = getAccountBalance(2);
        
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(String.format(Locale.ROOT, """
                       {
                        "senderAccountId": 1,
                        "receiverAccountId": 2,
                        "amount": %.2f
                        }
                       """, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorType));
        
        assertEquals(senderBalanceBefore, getAccountBalance(1), 0.01, 
            "Sender balance should not have changed");
        assertEquals(receiverBalanceBefore, getAccountBalance(2), 0.01, 
            "Receiver balance should not have changed");
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
        double transferAmount = 500.0;
        double senderBalanceBefore = getAccountBalance(1);
        double receiverBalanceBefore = getAccountBalance(account);
        
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization","Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(String.format(Locale.ROOT, """
                       {
                        "senderAccountId": 1,
                        "receiverAccountId": %d,
                        "amount": 500
                        }
                       """, account))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        
        assertEquals(senderBalanceBefore - transferAmount, getAccountBalance(1), 0.01, 
            "Sender balance should decrease");
        assertEquals(receiverBalanceBefore + transferAmount, getAccountBalance(account), 0.01, 
            "Receiver balance should increase");
    }
    public static Stream<Arguments> transferToValidAccount(){
        return Stream.of(
                Arguments.of(2),
                Arguments.of(3));
    }

    @Test
    public void userCannotTransferMoreThanAccountBalance(){
        double senderBalanceBefore = getAccountBalance(1);
        double receiverBalanceBefore = getAccountBalance(2);
        double transferAmount = senderBalanceBefore + 100.0;
        
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(String.format(Locale.ROOT, """
                       {
                        "senderAccountId": 1,
                        "receiverAccountId": 2,
                        "amount": %.2f
                        }
                       """, transferAmount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
        
        assertEquals(senderBalanceBefore, getAccountBalance(1), 0.01, 
            "Sender balance should not have changed");
        assertEquals(receiverBalanceBefore, getAccountBalance(2), 0.01, 
            "Receiver balance should not have changed");
    }
}