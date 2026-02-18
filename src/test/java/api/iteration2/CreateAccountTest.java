package api.iteration2;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.helpers.AccountHelper;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse createdAccount = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post();

        CreateAccountResponse accountFromList = AccountHelper.getCreateAccountResponseById(
                createdAccount.getId(),
                userRequest.getUsername(),
                userRequest.getPassword());

        ModelAssertions.assertThatModels(createdAccount, accountFromList).match();
    }
}