package ui.pages;

import api.generators.RandomData;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
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

    @Step("Сменить имя на случайное")
    public String changeName() {
        return changeName(RandomData.getName());
    }

    @Step("Сменить имя на {name}")
    public String changeName(String name) {
        enterNewName.clear();
        enterNewName.sendKeys(name);
        addUserButton.click();
        return name;
    }

    @Step("Получить отображаемое имя в поле")
    public String getDisplayedName() {
        return enterNewName.getValue();
    }

    @Step("Проверить отображаемое имя: {expected}")
    public EditPanel verifyDisplayedName(String expected) {
        enterNewName.shouldHave(Condition.value(expected));
        return this;
    }
}

