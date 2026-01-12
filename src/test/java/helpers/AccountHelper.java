package helpers;

import specs.RequestSpecs;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class AccountHelper {
    private AccountHelper() {}

    public static double getAccountBalance(int accountId, String username, String password) {
        List<Map<String, Object>> accounts = given()
                .spec(RequestSpecs.authAsUser(username, password))
                .get("api/v1/customer/accounts")
                .then()
                .extract()
                .jsonPath()
                .getList("$");

        return accounts.stream()
                .filter(acc -> {
                    Object id = acc.get("id");
                    return id != null && ((Number) id).intValue() == accountId;
                })
                .findFirst()
                .map(acc -> ((Number) acc.get("balance")).doubleValue())
                .orElse(0.0);
    }
}
