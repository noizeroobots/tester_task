package service;

import helper.EndPoints;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import service.dto.request.EntityRequest;
import service.dto.response.EntityResponse;

import java.util.List;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
public class TodoClient {

    private final RequestSpecification requestSpecification;

    @Step("Create a new TODO")
    public ValidatableResponse postTodo(EntityRequest body) {
        return given()
                .auth().preemptive().basic("admin", "admin")
                .spec(requestSpecification)
                .body(body)
                .when()
                .post(EndPoints.CONTEXT_PATH)
                .then();
    }

    @Step("Get a JSON list of TODOs")
    public List<EntityResponse> getTodo(int offset, int limit) {
        return given()
                .auth().preemptive().basic("admin", "admin")
                .spec(requestSpecification)
                .queryParam("offset", offset)
                .queryParam("limit", limit)
                .when()
                .get(EndPoints.CONTEXT_PATH)
                .then()
                .extract()
                .body()
                .jsonPath().getList(".", EntityResponse.class);
    }

    @Step("Update an existing TODO")
    public ValidatableResponse putTodo(EntityRequest body, final int id) {
        return given()
                .auth().preemptive().basic("admin", "admin")
                .spec(requestSpecification)
                .body(body)
                .when()
                .put(EndPoints.CONTEXT_PATH + "/" + id)
                .then();
    }

    @Step("Delete an existing TODO")
    public ValidatableResponse deleteTodo(final int id) {
        return given()
                .auth().preemptive().basic("admin", "admin")
                .spec(requestSpecification)
                .when()
                .delete(EndPoints.CONTEXT_PATH + "/" + id)
                .then();
    }
}