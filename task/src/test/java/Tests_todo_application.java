import config.BaseConfig;
import helper.BodyGenerator;
import helper.Helper;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.aeonbits.owner.ConfigFactory;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import service.TodoClient;
import service.dto.request.EntityRequest;
import service.dto.response.EntityResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Epic("Test TODO manager")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests_todo_application {

    private TodoClient todoClient;
    private final int DEFAULT_MAX_LIMIT = 1000;
    private final int DEFAULT_OFFSET = 0;

    @BeforeAll
    void setUp() {
        BaseConfig config = ConfigFactory.create(BaseConfig.class);
        RequestSpecification postRequestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .setBaseUri(config.todoHostname())
                .setPort(config.todoPort())
                .build();

        todoClient = new TodoClient(postRequestSpec);
    }

    @AfterEach
    void deleteAllTodos() {
        List<EntityResponse> entityResponse = todoClient.getTodo(DEFAULT_OFFSET, DEFAULT_MAX_LIMIT);
        for (EntityResponse response : entityResponse) {
            int deleteById = response.getId();
            todoClient.deleteTodo(deleteById);
        }
    }

    @Test
    @DisplayName("Create a new TODO")
    public void testPostEntity() {
        final int ID = Helper.getRandomId();
        Allure.step("Step 1. Create a new TODO", () -> {
            EntityRequest postBody = BodyGenerator.getPostEntity()
                    .withId(ID)
                    .withText(Helper.getRandomText())
                    .withCompleted(false)
                    .please();

            todoClient.postTodo(postBody)
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
        });

        Allure.step("Step 2. Get created TODO", () -> {
            List<EntityResponse> entityResponse = todoClient.getTodo(DEFAULT_OFFSET, DEFAULT_MAX_LIMIT);
            for (EntityResponse response : entityResponse) {
                assertEquals(response.getId(), ID);
            }
        });
    }

    @Test
    @DisplayName("Check the performance of the `POST /todos` endpoint")
    public void testPostPerformance() {
        final int QUANTITY_FOR_PERF = 100;
        AtomicReference<Double> AVARAGE_TIME = new AtomicReference<>((double) 0);
        ArrayList<EntityRequest> data = new ArrayList<>(QUANTITY_FOR_PERF);
        Allure.step("Data setup", () -> {
            for (int i = 0; i < QUANTITY_FOR_PERF; i++) {
                data.add(BodyGenerator
                        .getPostEntity()
                        .withId(Helper.getRandomId())
                        .withText(Helper.getRandomText())
                        .withCompleted(false)
                        .please());
            }

            ArrayList<Long> times = new ArrayList<>();
            for (int i = 0; i < QUANTITY_FOR_PERF; i++) {
                long begin = System.nanoTime();
                todoClient.postTodo(data.get(i))
                        .assertThat()
                        .statusCode(HttpStatus.SC_CREATED);
                long end = System.nanoTime();
                long time = end - begin;
                times.add(time);
            }
            System.out.println(times.size());
            double averageTime = 0;
            for (int i = 1; i < QUANTITY_FOR_PERF; i++) {
                averageTime += times.get(i);
                System.out.println("----> " + times.get(i));
            }
            averageTime = averageTime / (QUANTITY_FOR_PERF - 1);
            AVARAGE_TIME.set(averageTime);
            System.out.printf("Average POST time: %,.2f ns\n", averageTime);
        });

        Allure.step("Result of calculation", () -> {
            Allure.addAttachment("Average POST time: ", String.format("%,.2f ns\n", AVARAGE_TIME.get()));
        });
    }

    @Test
    @DisplayName("Get a JSON list of TODOs using limit")
    public void testGetEntityLimit() {
        final int QUANTITY_OF_TODO = 3;
        final int LIMIT = 2;
        Allure.step("Step 1. Create 3 new TODO", () -> {
            for (int i = 0; i < QUANTITY_OF_TODO; i++) {
                EntityRequest postBody = BodyGenerator.getPostEntity()
                        .withId(Helper.getRandomId())
                        .withText(Helper.getRandomText())
                        .withCompleted(false)
                        .please();

                todoClient.postTodo(postBody)
                        .assertThat()
                        .statusCode(HttpStatus.SC_CREATED);
            }
        });

        Allure.step("Step 2. Get a JSON list of TODOs using limit = 2", () -> {
            List<EntityResponse> entityResponse = todoClient.getTodo(DEFAULT_OFFSET, LIMIT);
            assertEquals(LIMIT, entityResponse.size());
        });
    }

    @Test
    @DisplayName("Get a JSON list of TODOs using offset")
    public void testGetEntityOffset() {
        final int QUANTITY_OF_TODO = 3;
        final int OFFSET = 2;
        Allure.step("Step 1. Create 3 new TODO", () -> {
            for (int i = 0; i < QUANTITY_OF_TODO; i++) {
                EntityRequest postBody = BodyGenerator.getPostEntity()
                        .withId(Helper.getRandomId())
                        .withText(Helper.getRandomText())
                        .withCompleted(false)
                        .please();

                todoClient.postTodo(postBody)
                        .assertThat()
                        .statusCode(HttpStatus.SC_CREATED);
            }
        });

        Allure.step("Step 2. Get a JSON list of TODOs using offset = 2", () -> {
            List<EntityResponse> entityResponse = todoClient.getTodo(OFFSET, DEFAULT_MAX_LIMIT);
            assertEquals(QUANTITY_OF_TODO - OFFSET, entityResponse.size());
        });
    }

    @Test
    @DisplayName("Update an existing TODO")
    public void testPutEntity() {
        final int ID = Helper.getRandomId();
        final String BEFORE_TEXT = "BEFORE";
        final String AFTER_TEXT = "AFTER";
        Allure.step("Step 1. Create a new TODO", () -> {
            EntityRequest postBody = BodyGenerator.getPostEntity()
                    .withId(ID)
                    .withText(BEFORE_TEXT)
                    .withCompleted(false)
                    .please();

            todoClient.postTodo(postBody)
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
        });

        Allure.step("Step 2. Get a JSON list of TODOs", () -> {
            List<EntityResponse> entityResponse = todoClient.getTodo(DEFAULT_OFFSET, DEFAULT_MAX_LIMIT);
            for (EntityResponse response : entityResponse) {
                assertEquals(response.getId(), ID);
                assertEquals(response.getText(), BEFORE_TEXT);
            }
        });

        Allure.step("Step 3. Update a new TODO", () -> {
            EntityRequest post = BodyGenerator.getPostEntity()
                    .withId(ID)
                    .withText(AFTER_TEXT)
                    .withCompleted(false)
                    .please();

            todoClient.putTodo(post, ID)
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK);
        });

        Allure.step("Step 4. Get an updated JSON list of TODOs", () -> {
            List<EntityResponse> entityResponse = todoClient.getTodo(DEFAULT_OFFSET, DEFAULT_MAX_LIMIT);
            for (EntityResponse response : entityResponse) {
                assertEquals(response.getId(), ID);
                assertEquals(response.getText(), AFTER_TEXT);
            }
        });
    }

    @Test
    @DisplayName("Delete an existing TODO")
    public void testDeleteEntity() {
        final int ID = Helper.getRandomId();
        Allure.step("Step 1. Create a new TODO", () -> {
            EntityRequest postBody = BodyGenerator.getPostEntity()
                    .withId(ID)
                    .withText(Helper.getRandomText())
                    .withCompleted(false)
                    .please();

            todoClient.postTodo(postBody)
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
        });

        Allure.step("Step 2. Delete an existing TODO", () -> {
            todoClient.deleteTodo(ID)
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        });

        Allure.step("Step 3. Check a deleted TODO", () -> {
            List<EntityResponse> entityResponse = todoClient.getTodo(DEFAULT_OFFSET, DEFAULT_MAX_LIMIT);
            for (int i = 0; i < entityResponse.size(); i++) {
                assertNotEquals(entityResponse.get(i).getId(), ID);
            }
        });
    }
}