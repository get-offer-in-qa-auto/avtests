package api.generators;

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

    private static String generateRandomString(String characters, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
