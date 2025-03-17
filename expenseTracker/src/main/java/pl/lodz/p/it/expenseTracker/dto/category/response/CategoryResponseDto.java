package pl.lodz.p.it.expenseTracker.dto.category.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CategoryResponseDto {

    private String id;

    private String name;

    private String color;

    private String description;

    private String groupId;

    private String groupName;

    private String sign;

    private String version;

}