package pl.lodz.p.it.expenseTracker.service;

import org.springframework.web.bind.annotation.PathVariable;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryResponseDto;

public interface CategoryQueryService {

    CategoryListResponseDto getCategories(String groupId);

    CategoryResponseDto getCategoryById(String id);
}
