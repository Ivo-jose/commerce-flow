package br.com.ivogoncalves.catalog_service.application.usecases.product;

import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductFilter;
import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductPageRequest;
import br.com.ivogoncalves.catalog_service.application.ports.output.product.ProductPageOut;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.BrandRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.CategoryRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.ProductRepository;
import br.com.ivogoncalves.catalog_service.domain.enums.DimensionUnit;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ResourceNotFoundException;
import br.com.ivogoncalves.catalog_service.domain.models.Brand;
import br.com.ivogoncalves.catalog_service.domain.models.BrandId;
import br.com.ivogoncalves.catalog_service.domain.models.Category;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;
import br.com.ivogoncalves.catalog_service.domain.models.Dimensions;
import br.com.ivogoncalves.catalog_service.domain.models.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductsUseCaseTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ListProductsUseCase useCase;

    @Captor
    private ArgumentCaptor<Set<BrandId>> brandIdsCaptor;

    @Captor
    private ArgumentCaptor<Set<CategoryId>> categoryIdsCaptor;

    @Nested
    @DisplayName("Success scenarios and empty state")
    class SuccessAndEmptyScenarios {

        @Test
        @DisplayName("It should correctly return ProductPageOut when products are found.")
        void shouldReturnProductPageOutCorrectlyWhenProductsAreFound() {
            // Arrange
            var filter = createEmptyFilter();
            var pageRequest = createPageRequest();

            var brandId = createBrandId();
            var categoryId = createCategoryId();

            var product = createProduct(brandId, categoryId);
            var productsPage = createPage(List.of(product), 0, 10, 1);

            var brand = createBrand(brandId, "Nike");
            var category = createCategory(categoryId, "Footwear");

            when(productRepository.findAllActive(filter, pageRequest))
                    .thenReturn(productsPage);

            when(brandRepository.findAllByIds(Set.of(brandId)))
                    .thenReturn(List.of(brand));

            when(categoryRepository.findAllByIds(Set.of(categoryId)))
                    .thenReturn(List.of(category));

            // Act
            ProductPageOut result = useCase.execute(filter, pageRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.page()).isEqualTo(0);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(1);

            var productOut = result.content().get(0);

            assertThat(productOut.id()).isEqualTo(product.getId().id());
            assertThat(productOut.name()).isEqualTo("Product Test");
            assertThat(productOut.brand()).isEqualTo("Nike");
            assertThat(productOut.category()).isEqualTo("Footwear");

            verify(productRepository).findAllActive(filter, pageRequest);
            verify(brandRepository).findAllByIds(Set.of(brandId));
            verify(categoryRepository).findAllByIds(Set.of(categoryId));
        }

        @Test
        @DisplayName("Should return an empty page without querying Brand and Category when no products are found.")
        void shouldReturnEmptyPageAndNotCallBrandOrCategoryRepositoriesWhenNoProductsAreFound() {
            // Arrange
            var filter = createEmptyFilter();
            var pageRequest = createPageRequest();
            var emptyPage = createPage(Collections.emptyList(), 0, 10, 0);

            when(productRepository.findAllActive(filter, pageRequest))
                    .thenReturn(emptyPage);

            // Act
            ProductPageOut result = useCase.execute(filter, pageRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.page()).isEqualTo(0);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();

            verify(productRepository).findAllActive(filter, pageRequest);
            verifyNoInteractions(brandRepository);
            verifyNoInteractions(categoryRepository);
        }
    }

    @Nested
    @DisplayName("Scenarios where references are not found")
    class ExceptionScenarios {

        @Test
        @DisplayName("It should throw a ResourceNotFoundException when a product's brand is not found.")
        void shouldThrowResourceNotFoundExceptionWhenBrandIsNotFound() {
            // Arrange
            var filter = createEmptyFilter();
            var pageRequest = createPageRequest();

            var brandId = createBrandId();
            var categoryId = createCategoryId();

            var product = createProduct(brandId, categoryId);
            var productsPage = createPage(List.of(product), 0, 10, 1);

            var category = createCategoryWithIdOnly(categoryId);

            when(productRepository.findAllActive(filter, pageRequest))
                    .thenReturn(productsPage);

            when(brandRepository.findAllByIds(Set.of(brandId)))
                    .thenReturn(Collections.emptyList());

            when(categoryRepository.findAllByIds(Set.of(categoryId)))
                    .thenReturn(List.of(category));

            // Act & Assert
            assertThatThrownBy(() -> useCase.execute(filter, pageRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product brand not found.");

            verify(productRepository).findAllActive(filter, pageRequest);
            verify(brandRepository).findAllByIds(Set.of(brandId));
            verify(categoryRepository).findAllByIds(Set.of(categoryId));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when a product category is not found")
        void shouldThrowResourceNotFoundExceptionWhenCategoryIsNotFound() {
            // Arrange
            var filter = createEmptyFilter();
            var pageRequest = createPageRequest();

            var brandId = createBrandId();
            var categoryId = createCategoryId();

            var product = createProduct(brandId, categoryId);
            var productsPage = createPage(List.of(product), 0, 10, 1);

            var brand = createBrandWithIdOnly(brandId);

            when(productRepository.findAllActive(filter, pageRequest))
                    .thenReturn(productsPage);

            when(brandRepository.findAllByIds(Set.of(brandId)))
                    .thenReturn(List.of(brand));

            when(categoryRepository.findAllByIds(Set.of(categoryId)))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> useCase.execute(filter, pageRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product category not found.");

            verify(productRepository).findAllActive(filter, pageRequest);
            verify(brandRepository).findAllByIds(Set.of(brandId));
            verify(categoryRepository).findAllByIds(Set.of(categoryId));
        }
    }

    @Nested
    @DisplayName("ID deduplication scenarios")
    class DeduplicationScenarios {

        @Test
        @DisplayName("You should pass only distinct brand IDs to the BrandRepository.")
        void shouldPassDistinctBrandIdsToBrandRepository() {
            // Arrange
            var filter = createEmptyFilter();
            var pageRequest = createPageRequest();

            var sharedBrandId = createBrandId();
            var categoryId1 = createCategoryId();
            var categoryId2 = createCategoryId();

            var product1 = createProduct(sharedBrandId, categoryId1);
            var product2 = createProduct(sharedBrandId, categoryId2);

            var productsPage = createPage(List.of(product1, product2), 0, 10, 2);

            var brand = createBrand(sharedBrandId, "Samsung");
            var category1 = createCategory(categoryId1, "TVs");
            var category2 = createCategory(categoryId2, "Smartphones");

            when(productRepository.findAllActive(filter, pageRequest))
                    .thenReturn(productsPage);

            when(brandRepository.findAllByIds(Set.of(sharedBrandId)))
                    .thenReturn(List.of(brand));

            when(categoryRepository.findAllByIds(Set.of(categoryId1, categoryId2)))
                    .thenReturn(List.of(category1, category2));

            // Act
            var result = useCase.execute(filter, pageRequest);

            // Assert
            assertThat(result.content()).hasSize(2);

            verify(brandRepository).findAllByIds(brandIdsCaptor.capture());

            assertThat(brandIdsCaptor.getValue())
                    .containsExactlyInAnyOrder(sharedBrandId);
        }

        @Test
        @DisplayName("You should pass only distinct category IDs to the CategoryRepository.")
        void shouldPassDistinctCategoryIdsToCategoryRepository() {
            // Arrange
            var filter = createEmptyFilter();
            var pageRequest = createPageRequest();

            var brandId1 = createBrandId();
            var brandId2 = createBrandId();
            var sharedCategoryId = createCategoryId();

            var product1 = createProduct(brandId1, sharedCategoryId);
            var product2 = createProduct(brandId2, sharedCategoryId);

            var productsPage = createPage(List.of(product1, product2), 0, 10, 2);

            var brand1 = createBrand(brandId1, "Adidas");
            var brand2 = createBrand(brandId2, "Puma");
            var category = createCategory(sharedCategoryId, "Sportswear");

            when(productRepository.findAllActive(filter, pageRequest))
                    .thenReturn(productsPage);

            when(brandRepository.findAllByIds(Set.of(brandId1, brandId2)))
                    .thenReturn(List.of(brand1, brand2));

            when(categoryRepository.findAllByIds(Set.of(sharedCategoryId)))
                    .thenReturn(List.of(category));

            // Act
            var result = useCase.execute(filter, pageRequest);

            // Assert
            assertThat(result.content()).hasSize(2);

            verify(categoryRepository).findAllByIds(categoryIdsCaptor.capture());

            assertThat(categoryIdsCaptor.getValue())
                    .containsExactlyInAnyOrder(sharedCategoryId);
        }
    }

    // ------------------------------------------------------------------
    // Ancillary methods
    // ------------------------------------------------------------------

    private ProductFilter createEmptyFilter() {
        return new ProductFilter(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    private ProductPageRequest createPageRequest() {
        return new ProductPageRequest(0, 10, null, null);
    }

    private BrandId createBrandId() {
        return new BrandId(UUID.randomUUID());
    }

    private CategoryId createCategoryId() {
        return new CategoryId(UUID.randomUUID());
    }

    private Product createProduct(BrandId brandId, CategoryId categoryId) {
        var dimensions = new Dimensions(
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("1"),
                DimensionUnit.CM
        );

        return Product.create(
                "Product Test",
                "Description Test",
                new BigDecimal("1000.00"),
                new BigDecimal("2.5"),
                dimensions,
                brandId,
                categoryId
        );
    }

    /**
     * Creates a "complete" Brand — used in scenarios where {@code ProductOut.from}
     * is effectively invoked, and therefore {@code getName()} is called.
     */
    private Brand createBrand(BrandId id, String name) {
        var brand = mock(Brand.class);
        when(brand.getId()).thenReturn(id);
        when(brand.getName()).thenReturn(name);
        return brand;
    }

    /**
     * Creates a "complete" Category — used in scenarios where {@code ProductOut.from}
     * is effectively invoked, and therefore {@code getName()} is called.
     */
    private Category createCategory(CategoryId id, String name) {
        var category = mock(Category.class);
        when(category.getId()).thenReturn(id);
        when(category.getName()).thenReturn(name);
        return category;
    }

    /**
     * Creates a Brand using only the {@code getId()} tag. Used in scenarios of
     * exception, where the use case throws before reaching {@code ProductOut.from}.
     * Avoid {@code UnnecessaryStubbingException} by keeping strict stubbing on.
     */
    private Brand createBrandWithIdOnly(BrandId id) {
        var brand = mock(Brand.class);
        when(brand.getId()).thenReturn(id);
        return brand;
    }

    /**
     * Creates a Category with only {@code getId()} stubbed. Used in scenarios.
     * exception case, where the use case throws an exception before reaching {@code ProductOut.from}.
     * Avoid {@code UnnecessaryStubbingException} by keeping strict stubbing on.
     */
    private Category createCategoryWithIdOnly(CategoryId id) {
        var category = mock(Category.class);
        when(category.getId()).thenReturn(id);
        return category;
    }

    private Page<Product> createPage(
            List<Product> content,
            int page,
            int size,
            long totalElements
    ) {
        return new PageImpl<>(
                content,
                PageRequest.of(page, size),
                totalElements
        );
    }
}