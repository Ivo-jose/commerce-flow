package br.com.ivogoncalves.catalog_service.application.ports.repositories;

import br.com.ivogoncalves.catalog_service.domain.models.Category;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;

import java.util.List;
import java.util.Optional;

/**
 * @author Ivo Gonçalves
 */
public interface CategoryRepository {

    List<Category> findAll();

    Optional<Category> findById(CategoryId categoryId);

    Category save(Category category);
}