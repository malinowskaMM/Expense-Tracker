package pl.lodz.p.it.expenseTracker.controller.query;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryResponseDto;
import pl.lodz.p.it.expenseTracker.service.CategoryQueryService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class CategoryQueryController {

    private final CategoryQueryService service;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<CategoryListResponseDto> getCategoriesInGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(service.getCategories(groupId));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable String id) {
        CategoryResponseDto response = service.getCategoryById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("ETag", response.getSign());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }
}