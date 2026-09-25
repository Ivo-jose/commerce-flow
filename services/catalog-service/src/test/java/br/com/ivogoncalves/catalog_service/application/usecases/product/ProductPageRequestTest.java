package br.com.ivogoncalves.catalog_service.application.usecases.product;

import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductPageRequest;
import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductSortDirection;
import br.com.ivogoncalves.catalog_service.application.ports.input.product.ProductSortField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Ivo Gonçalves
 */
public class ProductPageRequestTest {

    @Test
    @DisplayName("Should successfully create a ProductPageRequest when page = 0 and size = 20")
    void shouldCreateProductPageRequestSuccessfully() {
        // ARRANGE & ACT
        ProductPageRequest request = new ProductPageRequest(0, 20, ProductSortField.PRICE, ProductSortDirection.DESC);

        // ASSERT
        assertThat(request.page()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sortField()).isEqualTo(ProductSortField.PRICE);
        assertThat(request.sortDirection()).isEqualTo(ProductSortDirection.DESC);
    }

    @Test
    @DisplayName("It should throw an IllegalArgumentException when page is less than 0")
    void shouldThrowIllegalArgumentExceptionWhenPageIsNegative() {
        // ACT & ASSERT
        assertThatThrownBy(() -> new ProductPageRequest(-1, 20, ProductSortField.NAME, ProductSortDirection.ASC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The page value cannot be less than zero.");
    }

    @Test
    @DisplayName("It should throw an IllegalArgumentException when size is greater than 100")
    void shouldThrowIllegalArgumentExceptionWhenSizeIsGreaterThan100() {
        // ACT & ASSERT
        assertThatThrownBy(() -> new ProductPageRequest(0, 101, ProductSortField.NAME, ProductSortDirection.ASC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The size should be in the range of 1 to 100.");
    }

    @Test
    @DisplayName("It should assign ProductSortField.NAME by default when sortField is null.")
    void shouldSetDefaultSortFieldToNameWhenNull() {
        // ARRANGE & ACT
        ProductPageRequest request = new ProductPageRequest(0, 20, null, ProductSortDirection.DESC);

        // ASSERT
        assertThat(request.page()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sortField()).isEqualTo(ProductSortField.NAME);
        assertThat(request.sortDirection()).isEqualTo(ProductSortDirection.DESC);
    }

    @Test
    @DisplayName("It should assign ProductSortDirection.ASC by default when sortDirection is null.")
    void shouldSetDefaultSortDirectionToAscWhenNull() {
        // ARRANGE & ACT
        ProductPageRequest request = new ProductPageRequest(0, 20, ProductSortField.NAME, null);

        // ASSERT
        assertThat(request.page()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sortField()).isEqualTo(ProductSortField.NAME);
        assertThat(request.sortDirection()).isEqualTo(ProductSortDirection.ASC);
    }
}