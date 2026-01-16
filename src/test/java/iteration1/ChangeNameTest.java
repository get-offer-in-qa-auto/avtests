package iteration1;

import generators.RandomModelGenerator;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import models.CreateUserRequest;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeNameTest extends BaseTest {

    @Test
    public void userCanChangeNameTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        ChangeNameRequest changeNameRequest = RandomModelGenerator.generate(ChangeNameRequest.class);

        ChangeNameResponse changeNameResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .post(changeNameRequest);

        ModelAssertions.assertThatModels(changeNameRequest, changeNameResponse).match();
    }

    @ParameterizedTest
    @MethodSource("invalidName")
    public void userCanNotChangeToInvalidName(String invalidName, String errorType) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        ChangeNameResponse nameBeforeResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(invalidName)
                .build();

        var response = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsBadRequest("message", errorType))
                .post(changeNameRequest)
                .extract();
        
        // Verify error message if response is JSON
        String responseBody = response.body().asString();
        if (responseBody != null && responseBody.trim().startsWith("{")) {
            try {
                java.util.List<String> messages = response.jsonPath().getList("message");
                if (messages != null && !messages.contains(errorType)) {
                    throw new AssertionError("Expected error message '" + errorType + "' not found in response: " + messages);
                }
            } catch (Exception e) {
                // If JSON parsing fails, skip message verification
            }
        }

        ChangeNameResponse nameAfterResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        ModelAssertions.assertThatModels(nameBeforeResponse, nameAfterResponse).match();
    }

    public static Stream<Arguments> invalidName() {
        return Stream.of(
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("5414141alex", "Name must contain two words with letters only"),
                Arguments.of("5414141alex$$$", "Name must contain two words with letters only"),
                Arguments.of("alexpetrov", "Name must contain two words with letters only"));

    }
}
