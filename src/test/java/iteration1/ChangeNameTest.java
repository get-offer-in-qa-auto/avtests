package iteration1;


import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

import static io.restassured.RestAssured.given;

public class ChangeNameTest {
    @BeforeAll
    public static void setupRestAssured(){
        RestAssured.filters(
                List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));

    }
    @Test
    public void userCanChangeName(){
        String newName = "Alex Petrov";

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body("""
                                {
                        "name": "Alex Petrov"
                                }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Проверяем через GET, что имя изменилось
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo(newName));
    }
    @ParameterizedTest
    @MethodSource("invalidName")
    public void userCanNotChangeToInvalidName(String name, String errorType){
        // Получаем имя до попытки изменения
        String nameBefore = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        String requestBody = String.format(Locale.ROOT, """
                {
                
                  "name": "%s"
                }
                """, name);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization","Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .body(requestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorType));

        // Проверяем через GET, что имя НЕ изменилось
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTE6S2F0ZTIwMDAj")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo(nameBefore));
    }
    public static Stream<Arguments> invalidName(){
        return Stream.of(
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("5414141alex", "Name must contain two words with letters only"),
                Arguments.of("5414141alex$$$", "Name must contain two words with letters only"),
                Arguments.of("alexpetrov", "Name must contain two words with letters only"));

    }
}