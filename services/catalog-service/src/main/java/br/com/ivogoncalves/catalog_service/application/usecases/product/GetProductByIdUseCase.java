package br.com.ivogoncalves.catalog_service.application.usecases.product;

import br.com.ivogoncalves.catalog_service.application.ports.output.product.ProductOut;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.BrandRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.CategoryRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.ProductRepository;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ResourceNotFoundException;
import br.com.ivogoncalves.catalog_service.domain.models.ProductId;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Ivo Gonçalves
 */
@Service
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    public GetProductByIdUseCase(ProductRepository productRepository, BrandRepository brandRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductOut execute(UUID id) {
        if (id == null ) {
            throw new IllegalArgumentException("It is not possible to perform a query with a null ID value.");
        }
        var product =  productRepository.findById(new ProductId(id)).orElseThrow(
                () -> new ResourceNotFoundException("There aren't products for this id.")
        );

        var brand =  brandRepository.findById(product.getBrandId()).orElseThrow(
                () -> new ResourceNotFoundException("There aren't brands for this id.")
        );

        var category =  categoryRepository.findById(product.getCategoryId()).orElseThrow(
                () -> new ResourceNotFoundException("There aren't categories for this id.")
        );

        return ProductOut.from(product, brand.getName(), category.getName());
    }
}
