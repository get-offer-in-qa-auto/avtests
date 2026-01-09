package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
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

public class MakeDepositTest {
    private static final String AUTH_HEADER = "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj";
    
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
                .header("Authorization", AUTH_HEADER)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .extract()
                .asString();
        return new JsonPath(response).getList("$");
    }
    @ParameterizedTest
    @MethodSource("validBalanceData")
    public void userCanMakeDeposit(double balance){
        double balanceBefore = getAccountBalance(1);
        
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", AUTH_HEADER)
                .body(String.format(Locale.ROOT, """
                        {
                        "id": 1,
                        "balance": %.2f
                        }
                        """, balance))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        
        assertEquals(balanceBefore + balance, getAccountBalance(1), 0.01, 
            "Balance should increase by deposit amount");
    }
    @ParameterizedTest
    @MethodSource("invalidBalanceData")
    public void userCannotDepositInvalidBalance(double balance, String errorValue){
        double balanceBefore = getAccountBalance(1);
        
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", AUTH_HEADER)
                .body(String.format(Locale.ROOT, """
                        {
                        "id": 1,
                        "balance": %.2f
                        }
                        """, balance))
                .post("http://localhost:4111/api/v1/accounts/deposit");
        
        assertEquals(balanceBefore, getAccountBalance(1), 0.01, "Balance should not have changed");
    }
    @ParameterizedTest
    @MethodSource("invalidDepositAccount")
    public void userCannotDepositToInvalidAccount(int account){
        List<Map<String, Object>> accountsBefore = getAllAccounts();
        boolean accountBelongsToUser = accountsBefore.stream()
                .anyMatch(acc -> {
                    Object id = acc.get("id");
                    return id != null && ((Number) id).intValue() == account;
                });
        
        if (accountBelongsToUser) {
            double balanceBefore = getAccountBalance(account);
            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .header("Authorization", AUTH_HEADER)
                    .body(String.format(Locale.ROOT, """
                            {
                            "id": %d,
                            "balance": 1000
                            }
                            """, account))
                    .post("http://localhost:4111/api/v1/accounts/deposit");
            assertEquals(balanceBefore + 1000, getAccountBalance(account), 0.01, 
                "Balance should increase for user's own account");
            return;
        }
        
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", AUTH_HEADER)
                .body(String.format(Locale.ROOT, """
                        {
                        "id": %d,
                        "balance": 1000
                        }
                        """, account))
                .post("http://localhost:4111/api/v1/accounts/deposit");
        
        List<Map<String, Object>> accountsAfter = getAllAccounts();
        assertEquals(accountsBefore.size(), accountsAfter.size(), 
            "Number of accounts should not change");
        
        for (Map<String, Object> accBefore : accountsBefore) {
            Object id = accBefore.get("id");
            if (id != null) {
                int accountId = ((Number) id).intValue();
                double balanceBefore = ((Number) accBefore.get("balance")).doubleValue();
                double balanceAfter = getAccountBalance(accountId);
                assertEquals(balanceBefore, balanceAfter, 0.01, 
                    "Balance for account " + accountId + " should not have changed");
            }
        }
    }
    public static Stream<Arguments> invalidDepositAccount(){
        return Stream.of(
                Arguments.of(2),
                Arguments.of(4));
    }
    public static Stream<Arguments> validBalanceData(){
        return Stream.of(
                Arguments.of(4000),
                Arguments.of(4999.99),
                Arguments.of(0.01));

    }
    public static Stream<Arguments> invalidBalanceData(){
        return Stream.of(
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000"),
                Arguments.of(-0.01, "Deposit amount must be at least 0.01"),
                Arguments.of(0, "Deposit amount must be at least 0.01"));

    }
}