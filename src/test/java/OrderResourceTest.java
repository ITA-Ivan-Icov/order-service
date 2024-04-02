import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.bson.types.ObjectId;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class OrderResourceTest {
    private String createdOrderId;

    @BeforeEach
    void createOrder() {
        JsonObject orderJson = Json.createObjectBuilder()
                .add("id", ObjectId.get().toString())
                .add("clientName", "Test Order")
                .add("startDate", "Test Date")
                .add("endDate", "Test Date")
                .add("phoneNumber", "+386 71 452 534")
                .add("insurance", "Kasko")
                .add("email", "ivan.icov@student.um.si")
                .build();

        String response = RestAssured.given()
                .contentType("application/json")
                .body(orderJson.toString())
                .when()
                .post("/order")
                .then()
                .statusCode(Matchers.either(CoreMatchers.is(200)).or(CoreMatchers.is(204)))
                .extract()
                .asString();

        createdOrderId = orderJson.getString("id");
    }

    @Test
    void testCreateOrder() {
        JsonObject orderJson = Json.createObjectBuilder()
                .add("clientName", "New Test Order")
                .add("startDate", "New Test Start date")
                .add("endDate", "New Test End date")
                .add("phoneNumber", "+386 71 452 534")
                .add("insurance", "Kasko")
                .add("email", "ivan.icov@student.um.si")
                .build();

        RestAssured.given()
                .contentType("application/json")
                .body(orderJson.toString())
                .when()
                .post("/order")
                .then()
                .statusCode(204)
                .contentType("");

    }

    @Test
    void testGetAllOrders() {
        RestAssured.given()
                .when()
                .get("/order")
                .then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    void testUpdateOrder() {
        JsonObject updatedOrderJson = Json.createObjectBuilder()
                .add("id", createdOrderId)
                .add("clientName", "Updated Test Order")
                .add("startDate", "Updated Test Start date")
                .add("endDate", "Updated Test End date")
                .add("phoneNumber", "+386 71 452 534")
                .add("insurance", "Kasko")
                .add("email", "ivan.icov@student.um.si")
                .build();

        RestAssured.given()
                .contentType("application/json")
                .body(updatedOrderJson.toString())
                .when()
                .put("/order/{id}", createdOrderId)
                .then()
                .statusCode(204);
    }

    @Test
    void testGetOrderById() {
        String existingOrderId = "660ad2ee4c35ee455a982041";

        RestAssured.given()
                .when()
                .get("/order/{id}", existingOrderId)
                .then()
                .statusCode(404)
                .contentType("application/json");
    }

    @Test
    void testDeleteOrder() {
        assertNotNull(createdOrderId, "createdOrderId should not be null");

        RestAssured.given()
                .when()
                .delete("/order/{id}", createdOrderId)
                .then()
                .statusCode(204);
    }
}
