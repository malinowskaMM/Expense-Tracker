package pl.lodz.p.it.expenseTracker.service.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryResponseDto;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryServiceConverter {

    private final MessageSigner messageSigner;

    public CategoryListResponseDto toCategoryListResponseDto(final List<Category> categories) {
        return CategoryListResponseDto.builder()
                .categories(categories.stream().map(this::toCategoryResponseDto).toList())
                .build();
    }

    public CategoryResponseDto toCategoryResponseDto(final Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId().toString())
                .name(category.getName())
                .color(category.getColor())
                .description(category.getDescription())
                .groupId(category.getGroup().getId().toString())
                .groupName(category.getGroup().getName())
                .sign(messageSigner.sign(category))
                .version(String.valueOf(category.getVersion()))
                .build();
    }
}
