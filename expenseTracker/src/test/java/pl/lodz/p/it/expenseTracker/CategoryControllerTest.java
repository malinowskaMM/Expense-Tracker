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
import pl.lodz.p.it.expenseTracker.domain.entity.AccountGroupRole;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.domain.enums.GroupRoleEnum;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryResponseDto;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.AccountGroupRoleRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.GroupRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TokenRepository;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// activate automatic startup and stop of containers
@Testcontainers
// JPA drop and create table, good for testing
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@ActiveProfiles("test")
class CategoryControllerTest {

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

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
          "postgres:15-alpine"
  );

  @AfterEach
  public void clean() {
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

    Group group2 = new Group("group2");
    groupRepository.save(group2);
    AccountGroupRole role3 = new AccountGroupRole(user1, group2, GroupRoleEnum.USER);
    AccountGroupRole role4 = new AccountGroupRole(user2, group2, GroupRoleEnum.ADMIN);
    AccountGroupRole role5 = new AccountGroupRole(user3, group2, GroupRoleEnum.USER);
    accountGroupRoleRepository.saveAll(List.of(role3, role4, role5));
    Category group2DefaultCategory = new Category("Default category", "#000000",
            "Default category", true, group2);
    group2.setAccountGroupRoles(List.of(role3, role4, role5));
    group2.setCategories(List.of(group2DefaultCategory));

    groupRepository.saveAll(List.of(group1, group2));
  }

  @Test
  void shouldGetAllCategoriesInGivenGroup() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("groupId", groupId)
            .get("/api/categories/group/{groupId}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryListResponseDto.class);

    Assert.assertEquals(2, response.getCategories().size());
  }

  @Test
  void shouldGetCategoryById() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    Assert.assertEquals("Category example", response.getName());
    Assert.assertEquals( group.getId().toString(), response.getGroupId());
    Assert.assertEquals("#000000", response.getColor());
    Assert.assertEquals("Example of category", response.getDescription());
  }

  @Test
  void shouldNotGetCategoryById() {
    authorizeUser();
    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-2138")
            .get("/api/categories/category/{id}")
            .then().statusCode(400);
  }

  @Test
  void shouldNotGetCategoryByIdBecauseCategoryDoNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-2137")
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldCreateCategory() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();

    JSONObject request = new JSONObject();
    request.put("name", "Category in test");
    request.put("color", "#ff0000");
    request.put("description", "Description of category in test");
    request.put("groupId", group.getId().toString());
    request.put("accountId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldNotCreateCategoryBecauseOfInvalidNameValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();

    JSONObject request = new JSONObject();
    request.put("name", "C");
    request.put("color", "#ff0000");
    request.put("description", "Description of category in test");
    request.put("groupId", group.getId().toString());
    request.put("accountId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateCategoryBecauseOfInvalidColorValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();

    JSONObject request = new JSONObject();
    request.put("name", "Category in test");
    request.put("color", "#");
    request.put("description", "Description of category in test");
    request.put("groupId", group.getId().toString());
    request.put("accountId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateCategoryBecauseOfInvalidDescriptionValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();

    JSONObject request = new JSONObject();
    request.put("name", "Category in test");
    request.put("color", "#ff0000");
    request.put("description", "D");
    request.put("groupId", group.getId().toString());
    request.put("accountId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateCategoryBecauseOfInvalidGroupValidation() {
    authorizeUser();

    JSONObject request = new JSONObject();
    request.put("name", "Category in test");
    request.put("color", "#ff0000");
    request.put("description", "Description of category in test");
    request.put("accountId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateCategoryBecauseOfInvalidAccountIdValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();

    JSONObject request = new JSONObject();
    request.put("name", "Category in test");
    request.put("color", "#ff0000");
    request.put("description", "Description of category in test");
    request.put("groupId", group.getId().toString());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateCategoryBecauseUserIsNotAssignToGroupInWhichTryToAddCategory() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var userId = accountRepository.findAccountByEmail("user3@example.com").get().getId();

    JSONObject request = new JSONObject();
    request.put("name", "Category in test");
    request.put("color", "#ff0000");
    request.put("description", "Description of category in test");
    request.put("groupId", group.getId().toString());
    request.put("accountId", userId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotCreateCategoryBecauseGroupDoesNotExists() {
    authorizeUser();

    JSONObject request = new JSONObject();
    request.put("name", "Category in test");
    request.put("color", "#ff0000");
    request.put("description", "Description of category in test");
    request.put("groupId", "-1");
    request.put("accountId", authAccountId);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/categories/category")
            .then()
            .statusCode(400);
  }

  @Test
  @Order(0)
  @Disabled
  void shouldDeleteCategoryById() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .pathParam("accountId", authAccountId)
            .delete("/api/categories/category/{id}/by/{accountId}")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldNotDeleteCategoryByIdBecauseCategoryIsDefault() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(Category::isDefault).findFirst();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .pathParam("accountId", authAccountId)
            .delete("/api/categories/category/{id}/by/{accountId}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotDeleteCategoryByIdBecauseCategoryDoesNotExists() {
    authorizeUser();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-2137")
            .pathParam("accountId", authAccountId)
            .delete("/api/categories/category/{id}/by/{accountId}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotDeleteCategoryByIdBecauseAccountIsNotAssignToGroupWhichCategoryBelongTo() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();
    var userId = accountRepository.findAccountByEmail("user3@example.com").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .pathParam("accountId", userId)
            .delete("/api/categories/category/{id}/by/{accountId}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldChangeCategory() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", authAccountId);
    request.put("name", "New name for category");
    request.put("color", "#ff0000");
    request.put("description", "New description for category");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(200);

  }

  @Test
  void shouldNotChangeCategoryBecauseOfInvalidSign() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", authAccountId);
    request.put("name", "New name for category");
    request.put("color", "#ff0000");
    request.put("description", "New description for category");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotChangeCategoryBecauseOfInvalidVersion() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", authAccountId);
    request.put("name", "New name for category");
    request.put("color", "#ff0000");
    request.put("description", "New description for category");
    request.put("version", response.getVersion() + 1.0);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(422);
  }

  @Test
  void shouldNotChangeCategoryBecauseCategoryNotExists() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", authAccountId);
    request.put("name", "New name for category");
    request.put("color", "#ff0000");
    request.put("description", "New description for category");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", "-1")
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotChangeCategoryBecauseOfInvalidAccountIdValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", "-1");
    request.put("name", "New name for category");
    request.put("color", "#ff0000");
    request.put("description", "New description for category");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotChangeCategoryBecauseOfInvalidNameValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", authAccountId);
    request.put("name", "N");
    request.put("color", "#ff0000");
    request.put("description", "New description for category");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotChangeCategoryBecauseOfInvalidColorValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", authAccountId);
    request.put("name", "New name for category");
    request.put("color", "#");
    request.put("description", "New description for category");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotChangeCategoryBecauseOfInvalidDescriptionValidation() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", authAccountId);
    request.put("name", "New name for category");
    request.put("color", "#ff0000");
    request.put("description", "N");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldNotChangeCategoryBecauseOfAccountIsNotAssignToGroupWhichCategoryBelongTo() {
    authorizeUser();

    var group = groupRepository.findGroupByName("group1").get();
    var categoryId = group.getCategories().stream().filter(category -> !category.isDefault()).findFirst();
    var userId = accountRepository.findAccountByEmail("user3@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", categoryId.get().getId())
            .get("/api/categories/category/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", userId);
    request.put("name", "New name for category");
    request.put("color", "#ff0000");
    request.put("description", "New description for category");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", categoryId.get().getId())
            .patch("/api/categories/category/{id}")
            .then()
            .statusCode(400);
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
