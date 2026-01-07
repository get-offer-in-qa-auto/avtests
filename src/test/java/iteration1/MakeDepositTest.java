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
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

import static io.restassured.RestAssured.given;

public class MakeDepositTest {
    @BeforeAll
    public static void setupRestAssured(){
        RestAssured.filters(
                List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));

    }
    @ParameterizedTest
    @MethodSource("validBalanceData")
    public void userCanMakeDeposit(double balance){
        String requestBody = String.format(Locale.ROOT, """
                {
                "id": 1,
                "balance": %.2f
                }
                """, balance);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

    }
    @ParameterizedTest
    @MethodSource("invalidBalanceData")
    public void userCannotDepositInvalidBalance(double balance, String errorValue){
        String requestBody = String.format(Locale.ROOT, """
                {
                "id": 1,
                "balance": %.2f
                }
                """, balance);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));
    }
    @ParameterizedTest
    @MethodSource("invalidDepositAccount")
    public void userCannotDepositToInvalidAccount(int account){
        String requestBody = String.format(Locale.ROOT, """
                {
                "id": %d,
                "balance": 1000
                }
                """,account);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization","Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
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

