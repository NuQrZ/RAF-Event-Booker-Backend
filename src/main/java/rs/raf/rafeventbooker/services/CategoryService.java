package rs.raf.rafeventbooker.services;

import rs.raf.rafeventbooker.model.Category;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.repositories.categories.CategoriesRepository;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

public class CategoryService {
    @Inject
    private CategoriesRepository categoriesRepository;

    public Page<Category> getCategories(int page, int size) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : Math.min(size, 100);
        return categoriesRepository.getAllCategories(p, s);
    }

    public Optional<Category> getCategory(int categoryID) {
        return categoriesRepository.getCategoryByID(categoryID);
    }

    public Optional<Category> getCategoryByName(String categoryName) {
        String name = normalize(categoryName);
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return categoriesRepository.getCategoryByName(name);
    }

    public boolean categoryExists(String categoryName) {
        String name = normalize(categoryName);
        return name != null && !name.isBlank() && categoriesRepository.categoryExists(name);
    }

    public boolean hasEvents(int categoryID) {
        return categoriesRepository.hasEvents(categoryID);
    }

    public int createCategory(Category category) {
        if (category == null) {
            throw new BadRequestException("Category is required");
        }
        String name = normalize(category.getCategoryName());
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        if (categoriesRepository.categoryExists(name)) {
            throw new BadRequestException("Category already exists");
        }

        category.setCategoryName(name);
        try {
            int id = categoriesRepository.createCategory(category);
            if (id <= 0) {
                throw new BadRequestException("Failed to create category");
            }
            return id;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new BadRequestException("Category already exists");
            }
            throw e;
        }
    }

    public void updateCategory(Category category) {
        if (category == null) {
            throw new BadRequestException("Invalid category");
        }
        Category existing = categoriesRepository.getCategoryByID(category.getCategoryID())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        String newName = normalize(category.getCategoryName());
        if (newName == null || newName.isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        if (!existing.getCategoryName().equals(newName) && categoriesRepository.categoryExists(newName)) {
            throw new BadRequestException("Category name already exists");
        }

        category.setCategoryName(newName);
        int updated = categoriesRepository.updateCategory(category);
        if (updated != 1) {
            throw new NotFoundException("Category not found");
        }
    }

    public void deleteCategory(int categoryID) {
        if (categoriesRepository.hasEvents(categoryID)) {
            throw new BadRequestException("Cannot delete category with events");
        }
        boolean ok = categoriesRepository.deleteCategory(categoryID);
        if (!ok) {
            throw new NotFoundException("Category not found");
        }
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }
}
