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
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountGroupRole;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.domain.enums.GroupRoleEnum;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupEntityListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupEntityResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupUserListResponseDto;
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
public class GroupControllerTest {

  @LocalServerPort
  private Integer port;

  @Setter
  private String authToken = "";

  private Account user1;
  private Account user2;
  private Account user3;
  private Group group1;

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

    group1 = new Group("group1");
    groupRepository.save(group1);
    AccountGroupRole role1 = new AccountGroupRole(user1, group1, GroupRoleEnum.ADMIN);
    AccountGroupRole role2 = new AccountGroupRole(user2, group1, GroupRoleEnum.USER);
    accountGroupRoleRepository.saveAll(List.of(role1, role2));
    Category group1DefaultCategory = new Category("Default category", "#000000",
            "Default category", true, group1);
    group1.setAccountGroupRoles(List.of(role1, role2));
    group1.setCategories(List.of(group1DefaultCategory));

    Group group2 = new Group("group2");
    groupRepository.save(group2);
    AccountGroupRole role3 = new AccountGroupRole(user1, group2, GroupRoleEnum.USER);
    AccountGroupRole role4 = new AccountGroupRole(user2, group2, GroupRoleEnum.ADMIN);
    accountGroupRoleRepository.saveAll(List.of(role3, role4));
    Category group2DefaultCategory = new Category("Default category", "#000000",
            "Default category", true, group2);
    group2.setAccountGroupRoles(List.of(role3, role4));
    group2.setCategories(List.of(group2DefaultCategory));

    groupRepository.saveAll(List.of(group1, group2));
  }

  @Test
  void shouldSuccessfullyGetGroupsAccountBelongTo() {
    authorize();

    var accountId = accountRepository.findAccountByEmail("user1@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", accountId)
            .get("/api/groups/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupListResponseDto.class);

    Assert.assertEquals(2, response.getGroups().size());
  }

  @Test
  void shouldSuccessfullyGetCategoriesInAllGroupsAccountBelongTo() {
    authorize();

    var accountId = accountRepository.findAccountByEmail("user1@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", accountId)
            .get("/api/groups/all/categories/account/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(CategoryListResponseDto.class);

    Assert.assertEquals(2, response.getCategories().size());
  }


  @Test
  void shouldSuccessfullyGetAllGroups() {
    authorize();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .get("/api/groups")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityListResponseDto.class);

    Assert.assertEquals(2, response.getGroups().size());
  }

  @Test
  void shouldSuccessfullyGetGroupById() {
    authorize();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", group1.getId())
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);
  }

  @Test
  void shouldUnsuccessfullyGetGroupByIdBecauseNoExists() {
    authorize();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", "-2137")
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldSuccessfullyGetUsersInGroupById() {
    authorize();

    var groupId = groupRepository.findGroupByName("group1").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/{id}/users")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupUserListResponseDto.class);
  }

  @Test
  void shouldSuccessfullyGetUsersOutOfGroupById() {
    authorize();

    var groupId = groupRepository.findGroupByName("group1").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/{id}/out/users")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupUserListResponseDto.class);
  }

  @Test
  void shouldSuccessfullyCreateGroup() {
    authorizeUser();

    JSONObject request = new JSONObject();
    request.put("name", "GroupExample");
    request.put("accountsIds", List.of(user1.getId(), user2.getId()));
    request.put("ownerId", accountRepository.findAccountByEmail("user1@example.com").get().getId());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/groups/group")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldUnsuccessfullyCreateGroupBecauseOfInvalidName() {
    authorizeUser();

    JSONObject request = new JSONObject();
    request.put("name", "G");
    request.put("accountsIds", List.of(user1.getId(), user2.getId()));
    request.put("ownerId", accountRepository.findAccountByEmail("user1@example.com").get().getId());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/groups/group")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldUnsuccessfullyCreateGroupBecauseOfEmptyAccountsList() {
    authorizeUser();

    JSONObject request = new JSONObject();
    request.put("name", "GroupExample");
    request.put("ownerId", accountRepository.findAccountByEmail("user1@example.com").get().getId());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .post("/api/groups/group")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldSuccessfullyChangeGroup() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var ownerId = accountRepository.findAccountByEmail("user1@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", ownerId);
    request.put("groupName", "newName");
    request.put("accountsEmails", List.of(user2.getEmail()));
    request.put("sign", response.getSign());
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", groupId)
            .patch("/api/groups/group/{id}")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldUnsuccessfullyChangeGroupBecauseOfInvalidVersion() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var ownerId = accountRepository.findAccountByEmail("user1@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", ownerId);
    request.put("groupName", "newName");
    request.put("accountsEmails", List.of(user2.getEmail()));
    request.put("sign", response.getSign());
    request.put("version", response.getVersion() + 1.0);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", groupId)
            .patch("/api/groups/group/{id}")
            .then()
            .statusCode(422);
  }

  @Test
  void shouldUnsuccessfullyChangeGroupBecauseOfInvalidSign() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var ownerId = accountRepository.findAccountByEmail("user1@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", ownerId);
    request.put("groupName", "newName");
    request.put("accountsEmails", List.of(user2.getEmail()));
    request.put("sign", "response.getSign()");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", "response.getSign()")
            .body(request.toJSONString())
            .pathParam("id", groupId)
            .patch("/api/groups/group/{id}")
            .then()
            .statusCode(422);
  }

  @Test
  void shouldUnsuccessfullyChangeGroupBecauseOfGroupNotExists() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var ownerId = accountRepository.findAccountByEmail("user1@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", ownerId);
    request.put("groupName", "newName");
    request.put("accountsEmails", List.of(user2.getEmail()));
    request.put("sign", response.getSign());
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", "-1")
            .patch("/api/groups/group/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldUnsuccessfullyChangeGroupBecauseOfInvalidGroupOwner() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var ownerId = accountRepository.findAccountByEmail("user2@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", ownerId);
    request.put("groupName", "newName");
    request.put("accountsEmails", List.of(user2.getEmail()));
    request.put("sign", response.getSign());
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", groupId)
            .patch("/api/groups/group/{id}")
            .then()
            .statusCode(403);
  }

  @Test
  void shouldUnsuccessfullyChangeGroupBecauseOfInvalidName() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var ownerId = accountRepository.findAccountByEmail("user1@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("accountId", ownerId);
    request.put("groupName", "n");
    request.put("accountsEmails", List.of(user2.getEmail()));
    request.put("sign", response.getSign());
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", groupId)
            .patch("/api/groups/group/{id}")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldSuccessfullyLeaveGroup() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var accountId = accountRepository.findAccountByEmail("user2@example.com").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .body(accountId)
            .patch("/api/groups/group/{id}/leave")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldUnsuccessfullyLeaveGroup() {
    authorizeUser();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var accountId = accountRepository.findAccountByEmail("user3@example.com").get().getId();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .body(accountId)
            .patch("/api/groups/group/{id}/leave")
            .then()
            .statusCode(400);
  }

  @Test
  void shouldSuccessfullyChangeHeadOfGroup() {
    authorize();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var newOwnerId = accountRepository.findAccountByEmail("user2@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("newOwnerIds", List.of(newOwnerId));
    request.put("sign", response.getSign());
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .header("If-Match", response.getSign())
            .body(request.toJSONString())
            .pathParam("id", groupId)
            .patch("/api/groups/group/{id}/head")
            .then()
            .statusCode(200);
  }

  @Test
  void shouldUnsuccessfullyChangeHeadOfGroupBecauseOfInvalidSign() {
    authorize();

    var groupId = groupRepository.findGroupByName("group1").get().getId();
    var newOwnerId = accountRepository.findAccountByEmail("user2@example.com").get().getId();

    var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .pathParam("id", groupId)
            .get("/api/groups/group/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(GroupEntityResponseDto.class);

    JSONObject request = new JSONObject();
    request.put("newOwnerIds", List.of(newOwnerId));
    request.put("sign", "response.getSign()");
    request.put("version", response.getVersion());

    RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toJSONString())
            .pathParam("id", groupId)
            .patch("/api/groups/group/{id}/head")
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