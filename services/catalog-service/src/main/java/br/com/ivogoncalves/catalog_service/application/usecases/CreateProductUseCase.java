package br.com.ivogoncalves.catalog_service.application.usecases;

import br.com.ivogoncalves.catalog_service.application.ports.input.ProductIn;
import br.com.ivogoncalves.catalog_service.application.ports.output.ProductOut;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.BrandRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.CategoryRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.ProductRepository;
import br.com.ivogoncalves.catalog_service.domain.exceptions.InactiveBrandException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.InactiveCategoryException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ResourceNotFoundException;
import br.com.ivogoncalves.catalog_service.domain.models.BrandId;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;
import org.springframework.stereotype.Service;

/**
 * @author Ivo Gonçalves
 */
@Service
public class CreateProductUseCase {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CreateProductUseCase(BrandRepository brandRepository, CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public ProductOut execute(ProductIn productIn) {
        var brand = brandRepository.findById(new BrandId(productIn.brandId())).orElseThrow(
                () -> new ResourceNotFoundException("There aren't brands for this id.")
        );
        if (!brand.isActive())
            throw new InactiveBrandException("It is not permitted to create a product with an inactive brand.");

        var category  = categoryRepository.findById(new CategoryId(productIn.categoryId())).orElseThrow(
                () -> new ResourceNotFoundException("There aren't categories for this id.")
        );
        if (!category.isActive())
            throw new InactiveCategoryException("It is not permitted to create a product with an inactive category.");

        var product = productRepository.save(productIn.toDomain());
        return ProductOut.from(product, brand.getName(), category.getName());
    }
}
