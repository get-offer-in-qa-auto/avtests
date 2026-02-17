package ui.iteration2;

import api.common.annotations.UserSession;
import api.common.storage.SessionStorage;
import api.generators.RandomData;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Condition;
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
        ChangeNameResponse initialProfile = getProfile();
        String originalName = initialProfile.getName();

        EditPanel editProfilePage = new UserDashboard().changeName().getPage(EditPanel.class);
        String newName = editProfilePage.changeName();
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CHANGED_NAME_SUCCESSFULLY.getMessage());

        editProfilePage = new UserDashboard().changeName().getPage(EditPanel.class);
        editProfilePage.getEnterNewName().shouldHave(Condition.value(newName));

        ChangeNameResponse updatedProfile = getProfile();
        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.getName()).isEqualTo(newName);
        assertThat(updatedProfile.getName()).isNotEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotLeaveNameEmptyTest() {
        ChangeNameResponse initialProfile = getProfile();
        String originalName = initialProfile.getName();

        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName(" ");
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CHANGED_NAME_TO_BLANK_STRING.getMessage());

        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        assertThat(editProfilePage.getEnterNewName().getValue()).isEqualTo(originalName);

        ChangeNameResponse updatedProfile = getProfile();
        assertThat(updatedProfile.getName()).isEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotChangeNameWithNumbers() {
        ChangeNameResponse initialProfile = getProfile();
        String originalName = initialProfile.getName();

        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName(RandomData.getNameWithNumbers());
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITH_NUMBERS.getMessage());

        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        assertThat(editProfilePage.getEnterNewName().getValue()).isEqualTo(originalName);

        ChangeNameResponse updatedProfile = getProfile();
        assertThat(updatedProfile.getName()).isEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotChangeNameWithSymbols() {
        ChangeNameResponse initialProfile = getProfile();
        String originalName = initialProfile.getName();

        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName(RandomData.getNameWithSymbols());
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITH_SYMBOLS.getMessage());

        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        assertThat(editProfilePage.getEnterNewName().getValue()).isEqualTo(originalName);

        ChangeNameResponse updatedProfile = getProfile();
        assertThat(updatedProfile.getName()).isEqualTo(originalName);
    }

    @Test
    @UserSession
    public void userCanNotChangeNameWithSpace() {
        ChangeNameResponse initialProfile = getProfile();
        String originalName = initialProfile.getName();

        EditPanel editProfilePage = new UserDashboard().open().changeName().getPage(EditPanel.class);
        editProfilePage.changeName(RandomData.getNameWithoutSpace());
        editProfilePage.checkAlertMessageAndAccept(
                BankAlert.USER_CANNOT_CHANGE_NAME_WITHOUT_SPACE.getMessage());

        editProfilePage = new EditPanel().open().getPage(EditPanel.class);
        assertThat(editProfilePage.getEnterNewName().getValue()).isEqualTo(originalName);

        ChangeNameResponse updatedProfile = getProfile();
        assertThat(updatedProfile.getName()).isEqualTo(originalName);
    }

    private ChangeNameResponse getProfile() {
        return (ChangeNameResponse) new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0);
    }
}
