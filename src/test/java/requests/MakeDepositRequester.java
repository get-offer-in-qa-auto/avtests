package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.MakeDepositRequest;


import static io.restassured.RestAssured.given;

public class MakeDepositRequester extends Request<MakeDepositRequest> {
    public MakeDepositRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification){
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(MakeDepositRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);

    }
}
