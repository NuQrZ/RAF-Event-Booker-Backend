package rs.raf.rafeventbooker.repositories.categories;

import rs.raf.rafeventbooker.model.Category;
import rs.raf.rafeventbooker.model.Page;

import java.util.Optional;

public interface CategoriesRepository {
    Optional<Category> getCategoryByID(int categoryID);
    Optional<Category> getCategoryByName(String categoryName);
    Page<Category> getAllCategories(int page, int size);
    int createCategory(Category category);
    int updateCategory(Category category);
    boolean deleteCategory(int categoryID);
    boolean categoryExists(String categoryName);
    boolean hasEvents(int categoryID);
}
