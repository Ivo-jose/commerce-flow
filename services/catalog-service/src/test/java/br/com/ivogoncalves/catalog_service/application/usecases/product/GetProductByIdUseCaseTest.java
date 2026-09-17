package br.com.ivogoncalves.catalog_service.application.usecases.product;

import br.com.ivogoncalves.catalog_service.application.ports.output.product.ProductOut;
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
import br.com.ivogoncalves.catalog_service.domain.models.ProductId;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Ivo Gonçalves
 */
@ExtendWith(MockitoExtension.class)
class GetProductByIdUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private GetProductByIdUseCase getProductByIdUseCase;

    private UUID brandId;
    private UUID categoryId;
    private UUID productId;
    private Product product;
    private Brand brand;
    private Category category;

    @BeforeEach
    void setup() {
        brandId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        Dimensions dimensions = new Dimensions(
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("1"),
                DimensionUnit.CM
        );

        product = Product.create(
                "Notebook Pro",
                "High performance laptop",
                new BigDecimal("5000.00"),
                new BigDecimal("2.5"),
                dimensions,
                new BrandId(brandId),
                new CategoryId(categoryId)
        );

        productId = product.getId().id();
        brand = mock(Brand.class);
        category = mock(Category.class);
    }

    @Test
    @DisplayName("Should return ProductOut successfully when product, brand and category are found")
    void shouldReturnProductSuccessfully() {
        // GIVEN
        when(brand.getName()).thenReturn("Tech Brand");
        when(category.getName()).thenReturn("Electronics");

        when(productRepository.findById(new ProductId(productId))).thenReturn(Optional.of(product));
        when(brandRepository.findById(new BrandId(brandId))).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(new CategoryId(categoryId))).thenReturn(Optional.of(category));

        // WHEN
        ProductOut response = getProductByIdUseCase.execute(productId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.description()).isEqualTo("High performance laptop");
        assertThat(response.name()).isEqualTo("Notebook Pro");
        assertThat(response.basePrice()).isEqualByComparingTo("5000.00");
        assertThat(response.brand()).isEqualTo("Tech Brand");
        assertThat(response.category()).isEqualTo("Electronics");

        verify(productRepository).findById(new ProductId(productId));
        verify(brandRepository).findById(new BrandId(brandId));
        verify(categoryRepository).findById(new CategoryId(categoryId));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when id is null.")
    void shouldThrowExceptionWhenIdIsNull() {
        // WHEN / THEN
        assertThatThrownBy(() -> getProductByIdUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("It is not possible to perform a query with a null ID value.");

        verifyNoInteractions(productRepository, brandRepository, categoryRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found.")
    void shouldThrowExceptionWhenProductNotFound() {
        // GIVEN
        when(productRepository.findById(new ProductId(productId))).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> getProductByIdUseCase.execute(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't products for this id.");

        verify(productRepository).findById(new ProductId(productId));
        verifyNoInteractions(brandRepository, categoryRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when brand not found.")
    void shouldThrowExceptionWhenBrandNotFound() {
        // GIVEN
        when(productRepository.findById(new ProductId(productId))).thenReturn(Optional.of(product));
        when(brandRepository.findById(new BrandId(brandId))).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> getProductByIdUseCase.execute(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't brands for this id.");

        verify(productRepository).findById(new ProductId(productId));
        verify(brandRepository).findById(new BrandId(brandId));
        verifyNoInteractions(categoryRepository);

    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found.")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // GIVEN
        when(productRepository.findById(new ProductId(productId))).thenReturn(Optional.of(product));
        when(brandRepository.findById(new BrandId(brandId))).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(new CategoryId(categoryId))).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> getProductByIdUseCase.execute(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't categories for this id.");

        verify(productRepository).findById(new ProductId(productId));
        verify(brandRepository).findById(new BrandId(brandId));
        verify(categoryRepository).findById(new CategoryId(categoryId));

    }
}