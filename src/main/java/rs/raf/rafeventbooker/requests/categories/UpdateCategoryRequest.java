package rs.raf.rafeventbooker.requests.categories;

import javax.validation.constraints.NotBlank;

public record UpdateCategoryRequest(
        @NotBlank String categoryName,
        @NotBlank String categoryDescription
) {}
