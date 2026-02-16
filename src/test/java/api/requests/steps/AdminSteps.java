package api.requests.steps;

import api.common.utils.RetryUtils;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class AdminSteps {
    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        return userRequest;
    }

    public static List<CreateUserResponse> getAllUsers() {
        return RetryUtils.retry(
                "Get all admin users",
                () -> new ValidatedCrudRequester<CreateUserResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.requestReturnsOK()).getAll(CreateUserResponse[].class),
                users -> users != null,
                3,
                1000
        );
    }
}
