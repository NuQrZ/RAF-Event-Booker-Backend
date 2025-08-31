package rs.raf.rafeventbooker.requests.categories;

import javax.validation.constraints.*;

public record CreateCategoryRequest(
        @NotBlank String categoryName,
        @NotBlank String categoryDescription
) {}