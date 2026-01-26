package api.generators;

import java.util.Locale;
import java.util.Random;

public class RandomData {
    private static final Random random = new Random();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";

    private RandomData() {}

    public static String getUsername() {
        return generateRandomString(LETTERS, 10);
    }

    public static String getPassword() {
        return generateRandomString(LETTERS.toUpperCase(), 3) +
                generateRandomString(LETTERS.toLowerCase(), 5) +
                generateRandomString(NUMBERS, 3) + "$";
    }

    public static String getName() {
        int firstWordLength = random.nextInt(10) + 1; // от 1 до 10 символов
        int secondWordLength = random.nextInt(10) + 1; // от 1 до 10 символов
        return generateRandomString(LETTERS, firstWordLength) + " " +
                generateRandomString(LETTERS, secondWordLength);
    }

    public static String getDepositAmount() {
        double amount = 0.01 + (random.nextDouble() * 4999.99);
        return String.format(Locale.US, "%.2f", amount);
    }

    public static String getInvalidDepositAmount() {
        double amount = 5000.01 + (random.nextDouble() * 1000.0);
        return String.format(Locale.US, "%.2f", amount);
    }

    public static String getInvalidUsername() {
        int length = random.nextInt(2) + 1; // от 1 до 2 символов
        return generateRandomString(LETTERS, length);
    }

    public static String getNameWithNumbers() {
        int firstWordLength = random.nextInt(5) + 3; 
        int secondWordLength = random.nextInt(5) + 3; 
        return generateRandomString(LETTERS, firstWordLength) + " " +
                generateRandomString(LETTERS, secondWordLength) +
                generateRandomString(NUMBERS, random.nextInt(2) + 1); 
    }

    public static String getNameWithSymbols() {
        String symbols = "%$&@#";
        int firstWordLength = random.nextInt(5) + 3; 
        int secondWordLength = random.nextInt(5) + 3; 
        return generateRandomString(LETTERS, firstWordLength) + " " +
                generateRandomString(LETTERS, secondWordLength) +
                symbols.charAt(random.nextInt(symbols.length()));
    }

    public static String getNameWithoutSpace() {
        int totalLength = random.nextInt(10) + 5; 
        return generateRandomString(LETTERS, totalLength);
    }

    public static String getRecipientName() {
        return getName();
    }

    public static String getTransactionAmount() {
        double amount = 0.01 + (random.nextDouble() * 9999.98);
        return String.format(Locale.US, "%.2f", amount);
    }

    public static String getInvalidTransactionAmount() {
        double amount = 10000.01 + (random.nextDouble() * 1000.0);
        return String.format(Locale.US, "%.2f", amount);
    }

    private static String generateRandomString(String characters, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
