package api.requests.skelethon;

import api.models.BaseModel;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import api.models.CreateAccountResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import api.models.ChangeNameResponse;
import api.models.MakeDepositResponse;
import api.models.MakeTransactionResponse;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/api/v1/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/api/v1/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/api/v1/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/api/v1/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    PROFILE(
            "/api/v1/customer/profile",
            BaseModel.class,
            ChangeNameResponse.class
    ),

    DEPOSIT(
            "/api/v1/accounts/deposit",
            BaseModel.class,
            MakeDepositResponse.class

    ),

    TRANSFER(
            "/api/v1/accounts/transfer",
            BaseModel.class,
            MakeTransactionResponse.class
    );


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}