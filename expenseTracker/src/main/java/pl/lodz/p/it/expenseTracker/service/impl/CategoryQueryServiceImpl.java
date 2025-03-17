package pl.lodz.p.it.expenseTracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryResponseDto;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.group.GroupNotFoundException;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.GroupRepository;
import pl.lodz.p.it.expenseTracker.service.CategoryQueryService;
import pl.lodz.p.it.expenseTracker.service.converter.CategoryServiceConverter;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final GroupRepository groupRepository;
    private final LoggerService logger = new LoggerService();

    private final CategoryServiceConverter converter;

    private final Internationalization internationalization;

    @Override
    public CategoryListResponseDto getCategories(String groupId) {
        var group = groupRepository.findGroupById(Long.parseLong(groupId));

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + groupId + " has not been found.",
                    "CategoryQueryService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        logger.log("Query: getCategories executed for group id:" + groupId,
                "CategoryQueryService", LoggerService.LoggerServiceLevel.INFO);
        return converter.toCategoryListResponseDto(categoryRepository.findCategoriesByGroup(group.get()));
    }

    @Override
    public CategoryResponseDto getCategoryById(String id) {
        logger.log("Query: getCategoryById executed for id:" + id,
                "CategoryQueryService", LoggerService.LoggerServiceLevel.INFO);
        return converter.toCategoryResponseDto(categoryRepository.findCategoryById(Long.parseLong(id))
                .orElseThrow(() -> new CategoryNotFoundException(internationalization.getMessage("category.notFound", LocaleContextHolder.getLocale().getLanguage()))));
    }
}
