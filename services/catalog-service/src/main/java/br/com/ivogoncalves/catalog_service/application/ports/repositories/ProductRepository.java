package br.com.ivogoncalves.catalog_service.application.ports.repositories;

import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;
import br.com.ivogoncalves.catalog_service.domain.models.Product;
import br.com.ivogoncalves.catalog_service.domain.models.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * @author Ivo Gonçalves
 */
public interface ProductRepository {

    List<Product> findAll();

    List<Product> findAllByCategory(CategoryId categoryId);

    Optional<Product> findById(ProductId productId);

    Product save(Product product);
}