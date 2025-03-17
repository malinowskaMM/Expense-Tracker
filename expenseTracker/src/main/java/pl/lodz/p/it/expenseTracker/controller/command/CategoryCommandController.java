package pl.lodz.p.it.expenseTracker.controller.command;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.category.request.CategoryChangeRequestDto;
import pl.lodz.p.it.expenseTracker.dto.category.request.CategoryCreateRequestDto;
import pl.lodz.p.it.expenseTracker.service.CategoryCommandService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class CategoryCommandController {

    private final CategoryCommandService service;

    @DeleteMapping("/category/{id}/by/{accountId}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable String id, @PathVariable String accountId) {
        return ResponseEntity.ok(service.deleteCategory(id, accountId));
    }

    @PostMapping("/category")
    public ResponseEntity<Void> createCategory(@RequestBody @Valid CategoryCreateRequestDto request) {
        return ResponseEntity.ok(service.createCategory(
                request.getName(),
                request.getColor(),
                request.getDescription(),
                request.getGroupId(),
                request.getAccountId()));
    }

    @PatchMapping("/category/{id}")
    public ResponseEntity<Void> changeCategory(@PathVariable String id,
                                               @RequestBody @Valid CategoryChangeRequestDto request,
                                               @RequestHeader(value = "If-Match", required = true) String ifMatchHeader) {
        return ResponseEntity.ok(service.changeCategory(
                id,
                request.getName(),
                request.getColor(),
                request.getDescription(),
                request.getAccountId(),
                request.getVersion(),
                ifMatchHeader
        ));
    }
}