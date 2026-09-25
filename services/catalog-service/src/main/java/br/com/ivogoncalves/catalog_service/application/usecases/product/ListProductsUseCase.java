package br.com.ivogoncalves.catalog_service.application.usecases.product;

import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductFilter;
import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductPageRequest;
import br.com.ivogoncalves.catalog_service.application.ports.output.product.ProductOut;
import br.com.ivogoncalves.catalog_service.application.ports.output.product.ProductPageOut;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.BrandRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.CategoryRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.ProductRepository;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ResourceNotFoundException;
import br.com.ivogoncalves.catalog_service.domain.models.Brand;
import br.com.ivogoncalves.catalog_service.domain.models.BrandId;
import br.com.ivogoncalves.catalog_service.domain.models.Category;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ivo Gonçalves
 */
@Service
public class ListProductsUseCase {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ListProductsUseCase(BrandRepository brandRepository, CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public ProductPageOut execute(ProductFilter filter, ProductPageRequest pageRequest) {
        Set<BrandId> brandIds = new HashSet<>();
        Set<CategoryId> categoryIds = new HashSet<>();

        var productsPage = productRepository.findAllActive(filter, pageRequest);

        if (productsPage.isEmpty()) {
            return new ProductPageOut(
                List.of(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages()
            );
        }

        productsPage.getContent().forEach(p -> {
            brandIds.add(p.getBrandId());
            categoryIds.add(p.getCategoryId());
        });

        var brands = brandRepository.findAllByIds(brandIds);
        var categories = categoryRepository.findAllByIds(categoryIds);

        Map<BrandId, Brand> brandsById = brands.stream().collect(Collectors.toMap(Brand::getId, Function.identity()));
        Map<CategoryId, Category> categoriesById = categories.stream().collect(Collectors.toMap(Category::getId, Function.identity()));

        var productsOut = productsPage.getContent().stream()
                .map(product -> {
                    var brand = brandsById.get(product.getBrandId());
                    if (brand == null)
                        throw new ResourceNotFoundException("Product brand not found.");

                    var category = categoriesById.get(product.getCategoryId());
                    if (category == null)
                        throw new ResourceNotFoundException("Product category not found.");


                    return ProductOut.from(product, brand.getName(), category.getName());
                }).toList();

        return new ProductPageOut(
                productsOut,
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages()
        );
    }
}