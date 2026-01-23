package ui.pages;

import api.generators.RandomData;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class EditPanel extends BasePage<EditPanel> {
    private final SelenideElement editProfilePanel =  $(Selectors.byText("✏️ Edit Profile"));
    private final SelenideElement enterNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private final SelenideElement addUserButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public String changeName() {
        return changeName(RandomData.getName());
    }

    public String changeName(String name) {
        enterNewName.clear();
        enterNewName.sendKeys(name);
        addUserButton.click();
        return name;
    }
}

