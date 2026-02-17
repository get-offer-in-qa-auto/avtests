package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    PROFILE(
            "/customer/profile",
            BaseModel.class,
            ChangeNameResponse.class
    ),

    DEPOSIT(
            "/accounts/deposit",
            BaseModel.class,
            MakeDepositResponse.class

    ),

    TRANSFER(
            "/accounts/transfer",
            BaseModel.class,
            MakeTransactionResponse.class
    );


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}