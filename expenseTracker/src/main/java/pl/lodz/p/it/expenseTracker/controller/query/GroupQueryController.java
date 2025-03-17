package pl.lodz.p.it.expenseTracker.controller.query;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupEntityListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupEntityResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupUserListResponseDto;
import pl.lodz.p.it.expenseTracker.service.GroupQueryService;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class GroupQueryController {

    private final GroupQueryService service;

    @GetMapping
    public ResponseEntity<GroupEntityListResponseDto> getGroups() {
        return ResponseEntity.ok(service.getGroups());
    }

    @GetMapping("/group/{id}")
    public ResponseEntity<GroupEntityResponseDto> getGroupWithId(@PathVariable String id) {
        GroupEntityResponseDto response = service.getGroupWithId(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("ETag", response.getSign());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<GroupUserListResponseDto> getUsersInGroupWithId(@PathVariable String id) {
        return ResponseEntity.ok(service.getUsersInGroupWithId(id));
    }

    @GetMapping("/{id}/out/users")
    public ResponseEntity<GroupUserListResponseDto> getUsersOutOfGroupWithId(@PathVariable String id) {
        return ResponseEntity.ok(service.getUsersOutOfGroupWithId(id));
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<GroupListResponseDto> getGroupsAccountBelongTo(@PathVariable String id) {
        return ResponseEntity.ok(service.getGroupsAccountBelongTo(id));
    }

    @GetMapping("/all/categories/account/{id}")
    public ResponseEntity<CategoryListResponseDto> getCategoriesInAllGroupsAccountBelongTo(@PathVariable String id) {
        return ResponseEntity.ok(service.getCategoriesInAllGroupsAccountBelongTo(id));
    }
}