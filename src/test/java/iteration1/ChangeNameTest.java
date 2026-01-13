package iteration1;

import generators.RandomData;
import models.ChangeNameRequest;
import models.CreateUserRequest;
import models.UserRole;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;

public class ChangeNameTest extends BaseTest {

    @Test
    public void userCanChangeNameTest() {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(RandomData.getName())
                .build();

        new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(changeNameRequest);

        // Получаем имя из профиля после изменения
        String actualName = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        softly.assertThat(actualName).isEqualTo(changeNameRequest.getName());

    }

    @ParameterizedTest
    @MethodSource("invalidName")
    public void userCanNotChangeToInvalidName(String invalidName, String errorType) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated()).post(userRequest);
        new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);

        String nameBefore = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(invalidName)
                .build();

        new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorType))
                .post(changeNameRequest);

        String nameAfter = given()
                .spec(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()))
                .get("api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        softly.assertThat(nameAfter).isEqualTo(nameBefore);
    }

    public static Stream<Arguments> invalidName() {
        return Stream.of(
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("5414141alex", "Name must contain two words with letters only"),
                Arguments.of("5414141alex$$$", "Name must contain two words with letters only"),
                Arguments.of("alexpetrov", "Name must contain two words with letters only"));

    }
}
