package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        List<Map<String, Object>> accounts = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("api/v1/customer/accounts")
                .then()
                .extract()
                .jsonPath()
                .getList("$");

        softly.assertThat(accounts)
                .extracting(acc -> ((Number) acc.get("id")).intValue())
                .contains(accountId);
    }
}