package pl.lodz.p.it.expenseTracker;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TokenRepository;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// activate automatic startup and stop of containers
@Testcontainers
// JPA drop and create table, good for testing
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@ActiveProfiles("test")
class AuthenticationControllerTest {

  public static final String ADMIN_EXAMPLE_COM = "admin1@example.com";
  public static final String USER_1_EXAMPLE_COM = "user12@example.com";
  @LocalServerPort
  private Integer port;

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  TokenRepository tokenRepository;

  @Autowired
  PasswordEncoder encoder;

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
          "postgres:15-alpine"
  );

  @AfterEach
  public void clean() {
    tokenRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @BeforeEach
  public void prepare() {
    RestAssured.baseURI = "http://localhost:" + port;
    tokenRepository.deleteAll();
    accountRepository.deleteAll();

    Account admin = new Account(true, false, LocalDateTime.now(), "pl",
            ADMIN_EXAMPLE_COM, encoder.encode("password1"), AccountRoleEnum.ADMIN);

    Account user = new Account(true, false, LocalDateTime.now(), "en",
            USER_1_EXAMPLE_COM, encoder.encode("password1"));

    accountRepository.saveAll(List.of(admin, user));
  }


  @Test
  void shouldAuthorizeSuccessfullyAsAdmin() {
    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareAdminAuthorizeRequest(ADMIN_EXAMPLE_COM, "password1"))
            .post("/api/auth/account/authenticate")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountAuthenticationResponseDto.class);

    Assertions.assertNotNull(response);
    Assertions.assertEquals("ADMIN", response.getRole());
    Assertions.assertEquals("pl", response.getLanguage());
  }

  @Test
  void shouldAuthorizeUnsuccessfullyAsAdmin() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareAdminAuthorizeRequest(ADMIN_EXAMPLE_COM, "pa"))
            .post("/api/auth/account/authenticate")
            .then()
            .statusCode(403);
  }

  @Test
  void shouldRegisterSuccessfullyAsUser() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareUserRegisterRequest("exampleEmail@example.com", "Password2137!",
                    "Password2137!", "enUS"))
            .post("/api/auth/account/register")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldNotRegisterUserWithEmailAlreadyExists() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareUserRegisterRequest(USER_1_EXAMPLE_COM, "Password2137!",
                    "Password2137!", "en"))
            .post("/api/auth/account/register")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotRegisterUserWithInvalidEmailPattern() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareUserRegisterRequest("user1", "Password2137!",
                    "Password2137!", "en"))
            .post("/api/auth/account/register")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotRegisterUserWithInvalidPasswordPattern() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareUserRegisterRequest("userExample1@example.com", "p",
                    "p", "en"))
            .post("/api/auth/account/register")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotRegisterUserWithPasswordAndRepeatedPasswordDoNotMatch() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareUserRegisterRequest("userExample1@example.com", "Password2137!",
                    "Password21!", "en"))
            .post("/api/auth/account/register")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotRegisterUserWithInvalidLanguageValue() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareUserRegisterRequest("userExample1@example.com", "Password2137!",
                    "Password2137!", "ohoh"))
            .post("/api/auth/account/register")
            .then()
            .statusCode(400);
  }

  private String prepareAdminAuthorizeRequest(String email, String password) {
    JSONObject request = new JSONObject();
    request.put("email", email);
    request.put("password", password);
    return request.toJSONString();
  }

  private String prepareUserRegisterRequest(String email, String password, String repeatPassword, String lang) {
    JSONObject request = new JSONObject();
    request.put("email", email);
    request.put("password", password);
    request.put("repeatPassword", repeatPassword);
    request.put("language_", lang);
    request.put("callbackRoute", "");
    return request.toJSONString();
  }
}