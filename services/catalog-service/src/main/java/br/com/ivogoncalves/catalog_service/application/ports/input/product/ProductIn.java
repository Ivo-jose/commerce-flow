package br.com.ivogoncalves.catalog_service.application.ports.input.product;

import br.com.ivogoncalves.catalog_service.domain.models.BrandId;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;
import br.com.ivogoncalves.catalog_service.domain.models.Dimensions;
import br.com.ivogoncalves.catalog_service.domain.models.Product;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Ivo Gonçalves
 */
public record ProductIn(
        String name,
        String description,
        BigDecimal basePrice,
        BigDecimal weight,
        Dimensions dimensions,
        UUID brandId,
        UUID categoryId
) {

    public Product toDomain() {
        return Product.create(name, description, basePrice, weight, dimensions, new BrandId(brandId), new CategoryId(categoryId));
    }
}
