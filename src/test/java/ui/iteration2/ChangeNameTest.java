package ui.iteration2;

import api.common.annotations.UserSession;
import api.common.storage.SessionStorage;
import api.common.utils.RetryUtils;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.models.ChangeNameResponse;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditPanel;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanChangeNameTest() {
        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        String newName = editProfilePage.changeName();
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CHANGED_NAME_SUCCESSFULLY.getMessage());

        // Проверяем на UI, что имя изменилось
        editProfilePage = new UserDashboard().changeName().getPage(EditPanel.class);
        editProfilePage.verifyDisplayedName(newName);

        // Проверяем на API, что имя изменилось
        ChangeNameResponse updatedProfile = RetryUtils.retry(
                "Проверка обновления профиля через API",
                () -> {
                    ChangeNameResponse profile = new CrudRequester(
                            RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                            Endpoint.PROFILE,
                            ResponseSpecs.requestReturnsOK())
                            .getAll(ChangeNameResponse.class)
                            .extract()
                            .as(ChangeNameResponse.class);
                    return profile != null && profile.getName() != null && profile.getName().equals(newName) ? profile : null;
                },
                result -> result != null,
                10,
                1000L
        );
        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.getName()).isEqualTo(newName);
        assertThat(updatedProfile.getName()).isNotEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotLeaveNameEmptyTest() {
        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName(" ");

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.verifyDisplayedName(originalName);
        } else {
            assertThat(editProfilePage.getDisplayedName()).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotChangeNameWithNumbers() {
        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName("Alex Petrov93");
        editProfilePage.checkAlertMessageAndAccept(
                "Name must contain two words with letters only");

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.verifyDisplayedName(originalName);
        } else {
            assertThat(editProfilePage.getDisplayedName()).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotChangeNameWithSymbols() {
        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName("Alex Petrov%");
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITH_SYMBOLS.getMessage());

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.verifyDisplayedName(originalName);
        } else {
            assertThat(editProfilePage.getDisplayedName()).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotChangeNameWithSpace() {
        // Получаем исходное имя через API
        ChangeNameResponse initialProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String originalName = initialProfile != null ? initialProfile.getName() : null;

        // Меняем имя через UI
        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName("AlexPetrov");
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITHOUT_SPACE.getMessage());

        // Проверяем на UI, что имя не изменилось
        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        if (originalName != null && !originalName.isEmpty()) {
            editProfilePage.verifyDisplayedName(originalName);
        } else {
            assertThat(editProfilePage.getDisplayedName()).isNullOrEmpty();
        }

        // Проверяем на API, что имя не изменилось
        ChangeNameResponse updatedProfile = new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);
        String updatedName = updatedProfile != null ? updatedProfile.getName() : null;
        assertThat(updatedName).isEqualTo(originalName);
    }
}