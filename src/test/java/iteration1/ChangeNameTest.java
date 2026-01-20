package iteration1;

import generators.RandomData;
import models.ChangeNameRequest;
import models.CreateUserRequest;
import models.Profile;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import requests.CreateAccountRequester;
import requests.GetProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

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
                .post();

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(RandomData.getName())
                .build();

        new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(changeNameRequest);

        // Получаем имя из профиля после изменения
        Profile profile = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(Profile.class);

        softly.assertThat(profile.getName()).isEqualTo(changeNameRequest.getName());

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
                .post();

        Profile profileBefore = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(Profile.class);

        String nameBefore = profileBefore.getName();

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(invalidName)
                .build();

        new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorType))
                .post(changeNameRequest);

        Profile profileAfter = new GetProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(Profile.class);

        softly.assertThat(profileAfter.getName()).isEqualTo(nameBefore);
    }

    public static Stream<Arguments> invalidName() {
        return Stream.of(
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("5414141alex", "Name must contain two words with letters only"),
                Arguments.of("5414141alex$$$", "Name must contain two words with letters only"),
                Arguments.of("alexpetrov", "Name must contain two words with letters only"));

    }
}
