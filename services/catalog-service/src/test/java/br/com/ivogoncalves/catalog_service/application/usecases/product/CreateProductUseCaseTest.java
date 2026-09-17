package br.com.ivogoncalves.catalog_service.application.usecases.product;

import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductIn;
import br.com.ivogoncalves.catalog_service.application.ports.output.product.ProductOut;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.BrandRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.CategoryRepository;
import br.com.ivogoncalves.catalog_service.application.ports.repositories.ProductRepository;
import br.com.ivogoncalves.catalog_service.domain.enums.DimensionUnit;
import br.com.ivogoncalves.catalog_service.domain.exceptions.InactiveBrandException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.InactiveCategoryException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ResourceNotFoundException;
import br.com.ivogoncalves.catalog_service.domain.models.Brand;
import br.com.ivogoncalves.catalog_service.domain.models.BrandId;
import br.com.ivogoncalves.catalog_service.domain.models.Category;
import br.com.ivogoncalves.catalog_service.domain.models.CategoryId;
import br.com.ivogoncalves.catalog_service.domain.models.Dimensions;
import br.com.ivogoncalves.catalog_service.domain.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Ivo Gonçalves
 */
@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CreateProductUseCase createProductUseCase;

    private UUID brandId;
    private UUID categoryId;
    private ProductIn productIn;
    private Brand brand;
    private Category category;

    @BeforeEach
    void setUp() {
        brandId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        Dimensions dimensions = new Dimensions(
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("1"),
                DimensionUnit.CM
        );

        productIn = new ProductIn(
                "Notebook Pro",
                "High performance laptop",
                new BigDecimal("5000.00"),
                new BigDecimal("2.5"),
                dimensions,
                brandId,
                categoryId
        );

        brand = mock(Brand.class);
        category = mock(Category.class);
    }

    @Test
    @DisplayName("Should create product successfully when brand and category are active")
    void shouldCreateProductSuccessfully() {
        // GIVEN
        when(brand.getName()).thenReturn("Tech Brand");
        when(brand.isActive()).thenReturn(true);

        when(category.getName()).thenReturn("Electronics");
        when(category.isActive()).thenReturn(true);

        when(brandRepository.findById(any(BrandId.class)))
                .thenReturn(Optional.of(brand));

        when(categoryRepository.findById(any(CategoryId.class)))
                .thenReturn(Optional.of(category));

        Product savedProduct = productIn.toDomain();

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        // WHEN
        ProductOut response = createProductUseCase.execute(productIn);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.brand()).isEqualTo("Tech Brand");
        assertThat(response.category()).isEqualTo("Electronics");

        verify(brandRepository).findById(new BrandId(brandId));
        verify(categoryRepository).findById(new CategoryId(categoryId));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when brand is not found")
    void shouldThrowExceptionWhenBrandNotFound() {
        // GIVEN
        when(brandRepository.findById(any(BrandId.class)))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> createProductUseCase.execute(productIn))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't brands for this id.");

        verify(brandRepository).findById(new BrandId(brandId));
        verifyNoInteractions(categoryRepository, productRepository);
    }

    @Test
    @DisplayName("Should throw InactiveBrandException when brand is inactive")
    void shouldThrowExceptionWhenBrandIsInactive() {
        // GIVEN
        when(brand.isActive()).thenReturn(false);

        when(brandRepository.findById(any(BrandId.class)))
                .thenReturn(Optional.of(brand));

        // WHEN / THEN
        assertThatThrownBy(() -> createProductUseCase.execute(productIn))
                .isInstanceOf(InactiveBrandException.class)
                .hasMessage("It is not permitted to create a product with an inactive brand.");

        verify(brandRepository).findById(new BrandId(brandId));
        verifyNoInteractions(categoryRepository, productRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category is not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // GIVEN
        when(brand.isActive()).thenReturn(true);

        when(brandRepository.findById(any(BrandId.class)))
                .thenReturn(Optional.of(brand));

        when(categoryRepository.findById(any(CategoryId.class)))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> createProductUseCase.execute(productIn))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't categories for this id.");

        verify(categoryRepository).findById(new CategoryId(categoryId));
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw InactiveCategoryException when category is inactive")
    void shouldThrowExceptionWhenCategoryIsInactive() {
        // GIVEN
        when(brand.isActive()).thenReturn(true);
        when(category.isActive()).thenReturn(false);

        when(brandRepository.findById(any(BrandId.class)))
                .thenReturn(Optional.of(brand));

        when(categoryRepository.findById(any(CategoryId.class)))
                .thenReturn(Optional.of(category));

        // WHEN / THEN
        assertThatThrownBy(() -> createProductUseCase.execute(productIn))
                .isInstanceOf(InactiveCategoryException.class)
                .hasMessage("It is not permitted to create a product with an inactive category.");

        verifyNoInteractions(productRepository);
    }
}