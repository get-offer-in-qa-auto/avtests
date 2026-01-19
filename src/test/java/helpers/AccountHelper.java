package helpers;

import models.Account;
import requests.GetAccountsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;

public class AccountHelper {
    private AccountHelper() {}

    public static double getAccountBalance(int accountId, String username, String password) {
        Account[] accountsArray = new GetAccountsRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .body()
                .as(Account[].class);

        List<Account> accounts = Arrays.asList(accountsArray);

        return accounts.stream()
                .filter(acc -> acc.getId() == accountId)
                .findFirst()
                .map(Account::getBalance)
                .orElse(0.0);
    }
}
