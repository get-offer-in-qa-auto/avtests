package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.MakeTransactionRequest;


import static io.restassured.RestAssured.given;
public class MakeTransactionRequester extends Request<MakeTransactionRequest>{
    public MakeTransactionRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(MakeTransactionRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}

