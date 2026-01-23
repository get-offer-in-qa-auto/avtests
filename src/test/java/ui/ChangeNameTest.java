package ui;

import api.models.CreateUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Condition;
import models.ChangeNameResponse;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditPanel;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest extends BaseUiTest {

    @Test
    public void userCanChangeNameTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().changeName().getPage(EditPanel.class);
        String newName = editProfilePage.changeName();
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CHANGED_NAME_SUCCESSFULLY.getMessage());

        // Проверяем на UI, что имя изменилось
        editProfilePage = new UserDashboard().changeName().getPage(EditPanel.class);
        editProfilePage.getEnterNewName().shouldHave(Condition.value(newName));

        // Проверяем на API, что имя изменилось
        ChangeNameResponse updatedProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.getName()).isEqualTo(newName);
        assertThat(updatedProfile.getName()).isNotEqualTo(originalName);
    }

    @Test
    public void userCanNotLeaveNameEmptyTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName(" ");
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CHANGED_NAME_TO_BLANK_STRING.getMessage());

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.getEnterNewName().shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = editProfilePage.getEnterNewName().getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }

    @Test
    public void userCanNotChangeNameWithNumbers() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName("Alex Petrov93");
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITH_NUMBERS.getMessage());

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.getEnterNewName().shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = editProfilePage.getEnterNewName().getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }

    @Test
    public void userCanNotChangeNameWithSymbols() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName("Alex Petrov%");
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITH_SYMBOLS.getMessage());

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.getEnterNewName().shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = editProfilePage.getEnterNewName().getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }

    @Test
    public void userCanNotChangeNameWithSpace() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName("AlexPetrov");
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITHOUT_SPACE.getMessage());

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.getEnterNewName().shouldHave(Condition.value(originalName));
        } else {
            String displayedValue = editProfilePage.getEnterNewName().getValue();
            assertThat(displayedValue).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }
}