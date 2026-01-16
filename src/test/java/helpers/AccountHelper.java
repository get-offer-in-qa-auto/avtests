package helpers;

import models.CreateAccountResponse;
import models.MakeDepositResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Map;

public class AccountHelper {
    private AccountHelper() {}

    private static List<Map<String, Object>> getAccountsList(String username, String password) {
        var response = new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(0)
                .extract();
        
        
        List<Map<String, Object>> accounts = response.jsonPath().getList("customer.accounts");
        if (accounts == null || accounts.isEmpty()) {
            // Try accounts at root level
            accounts = response.jsonPath().getList("accounts");
        }
        if (accounts == null || accounts.isEmpty()) {
            Object customerObj = response.jsonPath().get("customer");
            if (customerObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> customer = (Map<String, Object>) customerObj;
                Object accountsObj = customer.get("accounts");
                if (accountsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> accountsList = (List<Map<String, Object>>) accountsObj;
                    accounts = accountsList;
                }
            }
        }
        if (accounts == null || accounts.isEmpty()) {
            throw new RuntimeException("Could not find accounts in PROFILE response. Response body: " + response.body().asString());
        }
        return accounts;
    }

    private static Map<String, Object> getAccountMap(int accountId, String username, String password) {
        List<Map<String, Object>> accounts = getAccountsList(username, password);

        return accounts.stream()
                .filter(acc -> {
                    Object id = acc.get("id");
                    return id != null && ((Number) id).intValue() == accountId;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
    }

    public static double getAccountBalance(int accountId, String username, String password) {
        Map<String, Object> account = getAccountMap(accountId, username, password);
        return ((Number) account.get("balance")).doubleValue();
    }

    public static MakeDepositResponse getAccountById(int accountId, String username, String password) {
        Map<String, Object> account = getAccountMap(accountId, username, password);

        Object accountNumberObj = account.get("accountNumber");
        String accountNumber;
        if (accountNumberObj instanceof String) {
            accountNumber = (String) accountNumberObj;
        } else if (accountNumberObj instanceof Number) {
            accountNumber = String.valueOf(((Number) accountNumberObj).intValue());
        } else {
            accountNumber = "";
        }

        return MakeDepositResponse.builder()
                .id(((Number) account.get("id")).intValue())
                .accountNumber(accountNumber)
                .balance(((Number) account.get("balance")).doubleValue())
                .transactions((List<Object>) account.get("transactions"))
                .build();
    }

    public static List<Integer> getAccountIds(String username, String password) {
        return getAccountsList(username, password).stream()
                .map(acc -> ((Number) acc.get("id")).intValue())
                .toList();
    }

    public static CreateAccountResponse getCreateAccountResponseById(long accountId, String username, String password) {
        Map<String, Object> account = getAccountsList(username, password).stream()
                .filter(acc -> {
                    Object id = acc.get("id");
                    return id != null && ((Number) id).longValue() == accountId;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        return CreateAccountResponse.builder()
                .id(((Number) account.get("id")).longValue())
                .accountNumber((String) account.get("accountNumber"))
                .balance(((Number) account.get("balance")).doubleValue())
                .transactions((List<String>) account.get("transactions"))
                .build();
    }
}
