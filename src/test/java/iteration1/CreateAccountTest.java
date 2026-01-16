package iteration1;

import helpers.AccountHelper;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse createdAccount = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        CreateAccountResponse accountFromList = AccountHelper.getCreateAccountResponseById(
                createdAccount.getId(),
                userRequest.getUsername(),
                userRequest.getPassword());

        ModelAssertions.assertThatModels(createdAccount, accountFromList).match();
    }
}