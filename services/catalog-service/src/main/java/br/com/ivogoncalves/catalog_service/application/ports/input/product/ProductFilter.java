package br.com.ivogoncalves.catalog_service.application.ports.input.product;

import br.com.ivogoncalves.catalog_service.domain.models.BrandId;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;

import java.util.Optional;

/**
 * @author Ivo Gonçalves
 */
public record ProductFilter(Optional<String> name, Optional<BrandId> brandId, Optional<CategoryId> categoryId) {
}
