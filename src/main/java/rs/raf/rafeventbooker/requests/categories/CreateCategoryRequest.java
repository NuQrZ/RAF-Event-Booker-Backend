package rs.raf.rafeventbooker.requests.categories;

import javax.validation.constraints.*;

public class CreateCategoryRequest {
    @NotBlank private String categoryName;
    @NotBlank private String categoryDescription;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }
}
