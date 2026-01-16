package requests.skelethon.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.Endpoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndpointInterface;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private final CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        var extract = crudRequester.post(model).extract();
        if (endpoint == Endpoint.PROFILE) {
            return (T) extract.jsonPath().getObject("customer", endpoint.getResponseModel());
        }
        return (T) extract.as(endpoint.getResponseModel());
    }

    @Override
    public T get(long id) {
        var extract = crudRequester.get(id).extract();
        // For PROFILE GET, response has flat structure, not nested in "customer"
        return (T) extract.as(endpoint.getResponseModel());
    }

    @Override
    public Object update(long id, BaseModel model) {
        return null;
    }

    @Override
    public Object delete(long id) {
        return null;
    }
}