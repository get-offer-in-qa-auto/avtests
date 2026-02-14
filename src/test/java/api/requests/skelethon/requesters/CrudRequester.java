package api.requests.skelethon.requesters;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface {
    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    @Step("POST запрос с телом {model}")
    public ValidatableResponse post(BaseModel model) {
        var body = model == null ? "" : model;
        return  given()
                .spec(requestSpecification)
                .body(body)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("GET запрос с id {id}")
    public Object get(long id) {
        return null;
    }

    @Override
    @Step("PUT запрос с id {id} и телом {model}")
    public ValidatableResponse update(long id, BaseModel model) {
        var body = model == null ? "" : model;
        String url = endpoint.getUrl();
    
        if (id > 0) {
            url = url + "/" + id;
        }
        return given()
                .spec(requestSpecification)
                .body(body)
                .put(url)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("DELETE запрос с id {id}")
    public Object delete(long id) {
        return null;
    }

    @Step("GET запрос для получения всех записей {clazz}")
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then().assertThat()
                .spec(responseSpecification);
    }
}