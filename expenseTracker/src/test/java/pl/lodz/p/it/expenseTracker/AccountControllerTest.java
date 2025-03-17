package pl.lodz.p.it.expenseTracker;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.junit.Assert;
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
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountResponseDto;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TokenRepository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// activate automatic startup and stop of containers
@Testcontainers
@ActiveProfiles("test")
// JPA drop and create table, good for testing
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
public class AccountControllerTest {

  @LocalServerPort
  private Integer port;

  @Setter
  private String authToken = "";

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  TokenRepository tokenRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  private DataSource administrationDataSource;

  @Autowired
  private DataSource trackingDataSource;

  @Container
  public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("test")
          .withUsername("test")
          .withPassword("test");


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
            "admin@example.com", encoder.encode("password1"), AccountRoleEnum.ADMIN);

    Account user = new Account(true, false, LocalDateTime.now(), "en",
            "user1@example.com", encoder.encode("password1"));

    accountRepository.saveAll(List.of(admin, user));
  }

  @Test
  void successfullyDisableAndNextEnableGivenAccount() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/disable")
            .then()
            .statusCode(200);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/enable")
            .then()
            .statusCode(200);
  }

  @Test
  void unsuccessfullyDisableDisabledAccount() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/disable")
            .then()
            .statusCode(200);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/disable")
            .then()
            .statusCode(400);
  }

  @Test
  void successfullyInactivateAndNextActivateGivenAccount() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/inactivate")
            .then()
            .statusCode(200);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/activate")
            .then()
            .statusCode(200);
  }

  @Test
  void unsuccessfullyInactivateInactivateAccount() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/inactivate")
            .then()
            .statusCode(200);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/inactivate")
            .then()
            .statusCode(400);
  }

  @Test
  void successfullyChangeGivenAccountMainLanguage() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    Assert.assertEquals("en", response.getLanguage());

    JSONObject request = new JSONObject();
    request.put("language", "plPL");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/language")
            .then()
            .statusCode(200);

    var responseAfterChanges = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    Assert.assertEquals("plPL", responseAfterChanges.getLanguage());
  }

  @Test
  void unsuccessfullyChangeGivenAccountMainLanguageBecauseOfInvalidVersion() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    Assert.assertEquals("en", response.getLanguage());

    JSONObject request = new JSONObject();
    request.put("language", "plPL");
    request.put("version", response.getVersion() + 1.0);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/language")
            .then()
            .statusCode(422);
  }

  @Test
  void unsuccessfullyChangeGivenAccountMainLanguageBecauseOfInvalidIfMatchHeaderValue() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    Assert.assertEquals("en", response.getLanguage());

    JSONObject request = new JSONObject();
    request.put("language", "plPL");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", "okok2137")
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/language")
            .then()
            .statusCode(422);
  }

  @Test
  @Disabled
  void successfullyChangePassword() {
    authorizeUser();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("lastPassword", "password1");
    request.put("newPassword", "Password1!");
    request.put("repeatedNewPassword", "Password1!");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/password")
            .then()
            .statusCode(200);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareAdminAuthorizeRequest("user1@example.com", "password1"))
            .post("/api/auth/account/authenticate")
            .then()
            .statusCode(403);

    var newResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareAdminAuthorizeRequest("user1@example.com", "Password1!"))
            .post("/api/auth/account/authenticate")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountAuthenticationResponseDto.class);

    Assertions.assertNotNull(newResponse.getAuthenticationToken());
  }

  @Test
  void unsuccessfullyChangePasswordLastPasswordMatchesNewPassword() {
    authorizeUser();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("lastPassword", "password1");
    request.put("newPassword", "password1");
    request.put("repeatedNewPassword", "password1");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/password")
            .then()
            .statusCode(400);
  }

  @Test
  void unsuccessfullyChangePasswordNewPasswordDoNotMatchRepeatedNewPassword() {
    authorizeUser();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("lastPassword", "password1");
    request.put("newPassword", "Password1!");
    request.put("repeatedNewPassword", "Password1");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/password")
            .then()
            .statusCode(400);
  }

  @Test
  void unsuccessfullyChangePasswordDueToInvalidVersionValue() {
    authorizeUser();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("lastPassword", "password1");
    request.put("newPassword", "Password1!");
    request.put("repeatedNewPassword", "Password1!");
    request.put("version", response.getVersion() + 1.0);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/password")
            .then()
            .statusCode(422);
  }

  @Test
  void unsuccessfullyChangePasswordDueToInvalidIfMatchHeaderValue() {
    authorizeUser();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("lastPassword", "password1");
    request.put("newPassword", "Password1!");
    request.put("repeatedNewPassword", "Password1!");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", "response.getSign()")
            .body(request.toJSONString())
            .pathParam("id", userId.get().getId())
            .patch("/api/accounts/account/{id}/password")
            .then()
            .statusCode(422);
  }

  @Test
  void successfullyGetAccounts() {
    authorize();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .get("/api/accounts")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountListResponseDto.class);

    Assert.assertEquals(2, response.getAccounts().size());
  }

  @Test
  void successfullyGetUsersAccounts() {
    authorize();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .get("/api/accounts/users")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountListResponseDto.class);

    Assert.assertEquals(1, response.getAccounts().size());
  }

  @Test
  void successfullyGetAccountById() {
    authorize();

    var userId = accountRepository.findAccountByEmail("user1@example.com");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", userId.get().getId())
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    Assert.assertEquals("user1@example.com", response.getEmail());
    Assert.assertEquals( "USER", response.getRole());
  }

  @Test
  void unsuccessfullyGetAccountById() {
    authorize();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-2137")
            .get("/api/accounts/account/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void successfullyGetAccountByEmail() {
    authorize();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .param("email", "user1@example.com")
            .get("/api/accounts/account")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountResponseDto.class);

    Assert.assertEquals("user1@example.com", response.getEmail());
    Assert.assertEquals( "USER", response.getRole());
  }

  @Test
  void unsuccessfullyGetAccountByEmail() {
    authorize();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .param("email", "user2137@example.com")
            .get("/api/accounts/account")
            .then()
            .statusCode(400);
  }

  private void authorize() {
    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareAdminAuthorizeRequest("admin@example.com", "password1"))
            .post("/api/auth/account/authenticate")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountAuthenticationResponseDto.class);
    setAuthToken(response.getAuthenticationToken());
  }

  private void authorizeUser() {
    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(prepareAdminAuthorizeRequest("user1@example.com", "password1"))
            .post("/api/auth/account/authenticate")
            .then()
            .statusCode(200)
            .extract()
            .as(AccountAuthenticationResponseDto.class);
    setAuthToken(response.getAuthenticationToken());
  }

  private String prepareAdminAuthorizeRequest(String email, String password) {
    JSONObject request = new JSONObject();
    request.put("email", email);
    request.put("password", password);
    return request.toJSONString();
  }
}