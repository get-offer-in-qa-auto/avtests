package requests.skelethon.requesters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.Endpoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndpointInterface;

import java.util.Map;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private final CrudRequester crudRequester;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        var extract = crudRequester.post(model).extract();
        if (endpoint == Endpoint.PROFILE) {
            Map<String, Object> responseMap = extract.body().as(Map.class);
            Map<String, Object> customerMap = (Map<String, Object>) responseMap.get("customer");
            return (T) objectMapper.convertValue(customerMap, endpoint.getResponseModel());
        }
        return (T) extract.as(endpoint.getResponseModel());
    }

    @Override
    public T get(long id) {
        var extract = crudRequester.get(id).extract();
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