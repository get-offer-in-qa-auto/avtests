package api;

import models.ChangeNameResponse;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.LoginUserRequest;
import models.comparison.ModelAssertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static specs.ResponseSpecs.AUTHORIZATION_HEADER;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(userRequest);

        ChangeNameResponse profileResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        org.assertj.core.api.Assertions.assertThat(profileResponse.getUsername()).isEqualTo(userRequest.getUsername());
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .header(AUTHORIZATION_HEADER, Matchers.notNullValue());

        ChangeNameResponse profileResponse = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);

        ModelAssertions.assertThatModels(userRequest, profileResponse).match();
    }
}
