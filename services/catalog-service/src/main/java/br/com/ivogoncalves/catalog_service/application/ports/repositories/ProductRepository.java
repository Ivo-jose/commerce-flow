package br.com.ivogoncalves.catalog_service.application.ports.repositories;

import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductFilter;
import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductPageRequest;
import br.com.ivogoncalves.catalog_service.domain.models.Product;
import br.com.ivogoncalves.catalog_service.domain.models.ProductId;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * @author Ivo Gonçalves
 */
public interface ProductRepository {

    Page<Product> findAllActive(ProductFilter productFilter, ProductPageRequest productPageRequest);

    Optional<Product> findById(ProductId productId);

    Product save(Product product);
}