package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    USER_DEPOSITED_SUCCESSFULLY("✅ Successfully deposited $"),
    USER_DEPOSITED_UNSUCCESSFULLY("❌ Please deposit less or equal to 5000$."),
    USER_TRANSFERRED_SUCCESSFULLY("✅ Successfully transferred $"),
    USER_TRANSFERRED_UNSUCCESSFULLY("❌ Error: Transfer amount cannot exceed 10000"),
    USER_CHANGED_NAME_SUCCESSFULLY("✅ Name updated successfully!"),
    USER_CHANGED_NAME_TO_BLANK_STRING("❌ Please enter a valid name."),
    USER_CANNOT_CHANGE_NAME_WITH_NUMBERS("❌ Please enter a valid name."),
    USER_CANNOT_CHANGE_NAME_WITH_SYMBOLS("Name must contain two words with letters only"),
    USER_CANNOT_CHANGE_NAME_WITHOUT_SPACE("Name must contain two words with letters only");






    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
