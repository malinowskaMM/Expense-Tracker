package pl.lodz.p.it.expenseTracker;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import pl.lodz.p.it.expenseTracker.domain.entity.*;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.domain.enums.GroupRoleEnum;
import pl.lodz.p.it.expenseTracker.domain.enums.PeriodUnitEnum;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.request.TransactionChangeRequestDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionResponseDto;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// activate automatic startup and stop of containers
@Testcontainers
// JPA drop and create table, good for testing
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@ActiveProfiles("test")
public class TransactionControllerTest {

  @LocalServerPort
  private Integer port;

  @Setter
  private String authToken = "";

  @Setter
  private String authAccountId = "";

  private Account user1;
  private Account user2;
  private Account user3;

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  TokenRepository tokenRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  GroupRepository groupRepository;

  @Autowired
  AccountGroupRoleRepository accountGroupRoleRepository;

  @Autowired
  CategoryRepository categoryRepository;

  @Autowired
  TransactionRepository transactionRepository;

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
          "postgres:15-alpine"
  );

  @AfterEach
  public void clean() {
    transactionRepository.deleteAll();
    accountGroupRoleRepository.deleteAll();
    tokenRepository.deleteAll();
    accountRepository.deleteAll();
    categoryRepository.deleteAll();
    groupRepository.deleteAll();
  }

  @BeforeEach
  public void prepare() {
    RestAssured.baseURI = "http://localhost:" + port;

    Account admin = new Account(true, false, LocalDateTime.now(), "pl",
            "admin@example.com", encoder.encode("password1"), AccountRoleEnum.ADMIN);

    user1 = new Account(true, false, LocalDateTime.now(), "en",
            "user1@example.com", encoder.encode("password1"));

    user2 = new Account(true, false, LocalDateTime.now(), "en",
            "user2@example.com", encoder.encode("password1"));

    user3 = new Account(true, false, LocalDateTime.now(), "en",
            "user3@example.com", encoder.encode("password1"));

    accountRepository.saveAll(List.of(admin, user1, user2, user3));

    Group group1 = new Group("group1");
    groupRepository.save(group1);
    AccountGroupRole role1 = new AccountGroupRole(user1, group1, GroupRoleEnum.ADMIN);
    AccountGroupRole role2 = new AccountGroupRole(user2, group1, GroupRoleEnum.USER);
    accountGroupRoleRepository.saveAll(List.of(role1, role2));
    Category group1DefaultCategory = new Category("Default category", "#000000",
            "Default category", true, group1);
    Category group1Category = new Category("Category example", "#000000",
            "Example of category", false, group1);
    group1.setCategories(List.of(group1DefaultCategory, group1Category));
    categoryRepository.saveAll(List.of(group1Category, group1DefaultCategory));
    groupRepository.saveAll(List.of(group1));

    Transaction expense1 = new Expense("Example expense 1", group1Category, true, 1,
            PeriodUnitEnum.MONTH, LocalDate.now().minusDays(2), BigDecimal.valueOf(100.0),
            "EXPENSE", user1);
    Transaction expense2 = new Expense("Example expense 2", group1Category, false, null,
            null, LocalDate.now().minusDays(8), BigDecimal.valueOf(10.0),
            "EXPENSE", user1);
    Transaction income1 = new Income("Example income 1", group1Category, false, null,
            null, LocalDate.now().minusDays(2), BigDecimal.valueOf(70.0), user1);
    Transaction income2 = new Income("Example income 2", group1Category, true, 1,
            PeriodUnitEnum.YEAR, LocalDate.now().minusDays(1), BigDecimal.valueOf(10000.0), user1);
    Transaction income3 = new Income("Example income 3", group1Category, true, 4,
            PeriodUnitEnum.DAY, LocalDate.now().minusDays(5), BigDecimal.valueOf(10.0), user1);

    transactionRepository.saveAll(List.of(expense1, expense2, income1, income2, income3));
  }

  @Test
  void shouldGetAllTransactionsInCategory() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("categoryId", categoryId.get().getId())
            .get("/api/transactions/category/{categoryId}")
            .then()
            .statusCode(200)
            .extract()
            .as(TransactionListResponseDto.class);

    Assert.assertEquals(5, response.getTransactions().size());
  }

  @Test
  void shouldNotGetAllTransactionsInCategoryBecauseCategoryDoesNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("categoryId", "-1")
            .get("/api/transactions/category/{categoryId}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldGetAllTransactionsByAccountId() {
    authorizeUser();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("accountId", authAccountId)
            .get("/api/transactions/account/{accountId}")
            .then()
            .statusCode(200)
            .extract()
            .as(TransactionListResponseDto.class);

    Assert.assertEquals(5, response.getTransactions().size());
  }

  @Test
  void shouldNotGetAllTransactionsByAccountIdBecauseAccountDoesNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("accountId", "-1")
            .get("/api/transactions/account/{accountId}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotGetTransactionsByIdBecauseTransactionsDoesNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-1")
            .get("/api/transactions/transaction/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldGetAllTransactionsInDateByAccountId() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("accountId", authAccountId)
            .pathParam("date", LocalDate.now().minusDays(2).format(formatter))
            .get("/api/transactions/account/{accountId}/byDate/{date}")
            .then()
            .statusCode(200)
            .extract()
            .as(TransactionListResponseDto.class);

    Assert.assertEquals(2, response.getTransactions().size());
  }

  @Test
  void shouldNotGetAllTransactionsInDateByAccountIdBecauseAccountDoesNotExists() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("accountId", "-1")
            .pathParam("date", LocalDate.now().minusDays(2).format(formatter))
            .get("/api/transactions/account/{accountId}/byDate/{date}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldCreateTransactionIncome() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);
    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst().get().getId();

    JSONObject request = new JSONObject();
    request.put("name", "Transaction income name");
    request.put("cycle", "CYCLE");
    request.put("period", 2);
    request.put("periodType", "MONTH");
    request.put("type", "INCOME");
    request.put("categoryId", categoryId);
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldCreateTransactionExpense() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);
    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst().get().getId();

    JSONObject request = new JSONObject();
    request.put("name", "Transaction expense name");
    request.put("cycle", "CYCLE");
    request.put("period", 2);
    request.put("periodType", "MONTH");
    request.put("type", "EXPENSE");
    request.put("categoryId", categoryId);
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldNotCreateTransactionBecauseOfInvalidNameValidation() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);
    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst().get().getId();

    JSONObject request = new JSONObject();
    request.put("name", "T");
    request.put("cycle", "CYCLE");
    request.put("period", 2);
    request.put("periodType", "MONTH");
    request.put("type", "INCOME");
    request.put("categoryId", categoryId);
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateTransactionBecauseOfInvalidCategoryValidation() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);

    JSONObject request = new JSONObject();
    request.put("name", "Transaction income name");
    request.put("cycle", "CYCLE");
    request.put("period", 2);
    request.put("periodType", "MONTH");
    request.put("type", "INCOME");
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateTransactionBecauseOfInvalidAmountValueValidation() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);
    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst().get().getId();

    JSONObject request = new JSONObject();
    request.put("name", "Transaction income name");
    request.put("cycle", "CYCLE");
    request.put("period", 2);
    request.put("periodType", "MONTH");
    request.put("type", "INCOME");
    request.put("categoryId", categoryId);
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(-100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(400);
  }


  @Test
  void shouldNotCreateTransactionBecauseOfCategoryDoesNotExists() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);

    JSONObject request = new JSONObject();
    request.put("name", "Transaction income name");
    request.put("cycle", "CYCLE");
    request.put("period", 2);
    request.put("periodType", "MONTH");
    request.put("type", "INCOME");
    request.put("categoryId", "-1");
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateTransactionBecauseOfTransactionIsCyclicAndPeriodIsNull() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);
    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst().get().getId();

    JSONObject request = new JSONObject();
    request.put("name", "Transaction income name");
    request.put("cycle", "CYCLE");
    request.put("periodType", "MONTH");
    request.put("type", "INCOME");
    request.put("categoryId", categoryId);
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateTransactionBecauseOfTransactionIsCyclicAndPeriodTypeIsNull() {
    authorizeUser();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(2);
    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst().get().getId();

    JSONObject request = new JSONObject();
    request.put("name", "Transaction income name");
    request.put("cycle", "CYCLE");
    request.put("period", 2);
    request.put("type", "INCOME");
    request.put("categoryId", categoryId);
    request.put("date", zonedDateTime.format(formatter));
    request.put("value", BigDecimal.valueOf(100.0));
    request.put("creatorId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/transactions/transaction")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldDeleteTransaction() {
    authorizeUser();

    var transactionId = transactionRepository.findTransactionByName("Example expense 1").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .get("/api/transactions/transaction/{id}")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldNotDeleteTransactionBecauseOfTransactionWithGivenIdDoesNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-1")
            .get("/api/transactions/transaction/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldStopRecurringTransaction() {
    authorizeUser();

    var transactionId = transactionRepository.findTransactionByName("Example expense 1").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/stop-recurring")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldNotStopRecurringTransactionBecauseOfTransactionWithGivenIdDoesNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-1")
            .patch("/api/transactions/transaction/{id}/stop-recurring")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotStopRecurringTransactionBecauseOfTransactionWithGivenIdIsNotCyclic() {
    authorizeUser();

    var transactionId = transactionRepository.findTransactionByName("Example expense 2").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/stop-recurring")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotStopRecurringTransactionBecauseOfTransactionIsAlreadyStopped() {
    authorizeUser();

    var transactionId = transactionRepository.findTransactionByName("Example expense 1").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/stop-recurring")
            .then()
            .statusCode(200);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/stop-recurring")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldRenewRecurringTransaction() {
    authorizeUser();

    var transactionId = transactionRepository.findTransactionByName("Example expense 1").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/stop-recurring")
            .then()
            .statusCode(200);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/renew-recurring")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldNotRenewRecurringTransactionBecauseOfTransactionIsAlreadyRenewed() {
    authorizeUser();

    var transactionId = transactionRepository.findTransactionByName("Example expense 1").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/renew-recurring")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotRenewRecurringTransactionBecauseOfTransactionWithGivenIdDoesNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-1")
            .patch("/api/transactions/transaction/{id}/renew-recurring")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotRenewRecurringTransactionBecauseOfTransactionWithGivenIdIsNotCyclic() {
    authorizeUser();

    var transactionId = transactionRepository.findTransactionByName("Example expense 2").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .patch("/api/transactions/transaction/{id}/renew-recurring")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldChangeTransaction() {
    authorizeUser();

    var transaction = transactionRepository.findTransactionByName("Example income 3").get();
    var transactionId = transaction.getId();

    var group = groupRepository.findGroupByName("group1").get();

    var transactionResponse = RestAssured.given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", transactionId)
            .when()
            .get("/api/transactions/transaction/{id}")
            .then()
            .statusCode(200).extract().as(TransactionResponseDto.class);


    TransactionChangeRequestDto requestDto = new TransactionChangeRequestDto();
    requestDto.setName("New Transaction Name");
    requestDto.setCycle("CYCLE");
    requestDto.setPeriod(BigDecimal.valueOf(1));
    requestDto.setPeriodType("MONTH");
    requestDto.setType("EXPENSE");
    requestDto.setCategoryId(group.getCategories().get(0).getId().toString());
    requestDto.setDate(LocalDate.now().toString());
    requestDto.setValue(BigDecimal.valueOf(50.0));
    requestDto.setVersion(transactionResponse.getVersion());


    RestAssured.given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", transactionResponse.getSign())
            .body(requestDto)
            .pathParam("id", transactionId)
            .when()
            .patch("/api/transactions/transaction/{id}")
            .then()
            .statusCode(200);
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
    setAuthAccountId(response.getId());
    setAuthToken(response.getAuthenticationToken());
  }


  private String prepareAdminAuthorizeRequest(String email, String password) {
    JSONObject request = new JSONObject();
    request.put("email", email);
    request.put("password", password);
    return request.toJSONString();
  }
}