package pl.lodz.p.it.expenseTracker.dto.category.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CategoryListResponseDto {

    private List<CategoryResponseDto> categories;
}
