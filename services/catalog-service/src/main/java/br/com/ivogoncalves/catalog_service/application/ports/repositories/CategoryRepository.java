package br.com.ivogoncalves.catalog_service.application.ports.repositories;

import br.com.ivogoncalves.catalog_service.domain.models.Category;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Ivo Gonçalves
 */
public interface CategoryRepository {

    List<Category> findAllByIds(Set<CategoryId> categoryIds);

    Optional<Category> findById(CategoryId categoryId);

    Category save(Category category);
}