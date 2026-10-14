package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.enums.ImageType;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ProductImageLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Ivo Gonçalves
 */
public class ProductImageTest {

    private ProductImage productImageForTests;


    @BeforeEach
    void setup() {
        productImageForTests = ProductImage.create("https://storageImages.com/photo1.png", 0);
    }

    @Test
    @DisplayName("Should create a product image")
    void shouldCreateProductImage() {
        // Assert
        assertThat(productImageForTests.getId().id()).isNotNull();
        assertThat(productImageForTests.getUrl()).isEqualTo("https://storageImages.com/photo1.png");
        assertThat(productImageForTests.getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productImageForTests.getOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when creating a Product Image with a null URL value.")
    void shouldThrowIllegalArgumentExceptionWhenCreatingProductImageWithNullUrl() {
        // Act / Assert
        assertThatThrownBy(() ->
                ProductImage.create(
                        null,
                        1
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The URL cannot be a null value.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when creating a Product Image with a empty URL value.")
    void shouldThrowIllegalArgumentExceptionWhenCreatingProductImageWithEmptyUrl() {
        // Act / Assert
        assertThatThrownBy(() ->
                ProductImage.create(
                        "",
                        1
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The URL cannot be an empty value or whitespace.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when creating a Product Image with a blank URL value.")
    void shouldThrowIllegalArgumentExceptionWhenCreatingProductImageWithBlankUrl() {
        // Act / Assert
        assertThatThrownBy(() ->
                ProductImage.create(
                        "  ",
                        1
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The URL cannot be an empty value or whitespace.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when url does match pattern")
    void shouldThrowIllegalArgumentExceptionWhenUrlDoesNotMatchPattern() {
        // Act/ Assert
        assertThatThrownBy(() ->
                ProductImage.create(
                        "https://example.com/imagem",
                        1
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The URL provided is invalid.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when creating a Product Image with a null order value.")
    void shouldThrowIllegalArgumentExceptionWhenCreatingProductImageWithNullOrder() {
        // Act / Assert
        assertThatThrownBy(() ->
                ProductImage.create(
                        "https://storageImages.com/photo1.png",
                        null
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must be a non-null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when creating a Product Image with a order value less than zero.")
    void shouldThrowIllegalArgumentExceptionWhenCreatingProductImageWithValueOrderLessThanZero() {
        // Act / Assert
        assertThatThrownBy(() ->
                ProductImage.create(
                        "https://storageImages.com/photo1.png",
                        -1
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must be a positive number.");
    }

    @Test
    @DisplayName("Should throw an ProductImageLimitExceeded exception when creating a Product Image with a order value greater than three.")
    void shouldThrowProductImageLimitExceededExceptionWhenCreatingProductImageWithValueOrderGreaterThanThree() {
        // Act / Assert
        assertThatThrownBy(() ->
                ProductImage.create(
                        "https://storageImages.com/photo1.png",
                        4
                ))
                .isInstanceOf(ProductImageLimitExceededException.class)
                .hasMessage("The image order cannot exceed the maximum limit of 4.");
    }

    @Test
    @DisplayName("Should successfully update the ProductImage order.")
    void shouldSuccessfullyUpdateProductImageOrder() {
        // Act
        productImageForTests.updateOrder(1);

        // Assert
        assertThat(productImageForTests.getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productImageForTests.getOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when updating the new order is the same as the old one.")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingOrderToSameValue() {
        //Act / Assert
        assertThatThrownBy(() ->
                productImageForTests.updateOrder(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating the new order to be identical to the old order is not permitted.");
    }

    @Test
    @DisplayName("Should throw when updating order to null")
    void shouldThrowWhenUpdatingOrderToNull() {
        assertThatThrownBy(() -> productImageForTests.updateOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must be a non-null.");

        assertThat(productImageForTests.getOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw when updating order above the maximum")
    void shouldThrowWhenUpdatingOrderAboveMaximum() {
        assertThatThrownBy(() -> productImageForTests.updateOrder(4))
                .isInstanceOf(ProductImageLimitExceededException.class)
                .hasMessage("The image order cannot exceed the maximum limit of 4.");

        assertThat(productImageForTests.getOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should allow the maximum valid order")
    void shouldAllowMaximumValidOrder() {
        productImageForTests.updateOrder(3);

        assertThat(productImageForTests.getOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should throw when updating order to a negative value")
    void shouldThrowWhenUpdatingOrderToNegativeValue() {
        assertThatThrownBy(() -> productImageForTests.updateOrder(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must be a positive number.");

        assertThat(productImageForTests.getOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should successfully switch galleries to main.")
    void shouldSuccessfullySwitchGalleryToMain() {
        // Act
        productImageForTests.switchToMain();

        // Asserts
        assertThat(productImageForTests.getType()).isEqualTo(ImageType.MAIN);
    }

    @Test
    @DisplayName("Should throw an IllegalState exception when switch from main to main")
    void shouldThrowIllegalStateExceptionWhenSwitchFromMainToMain() {
        // Setting the stage
        productImageForTests.switchToMain();

        // Act / Assert
        assertThatThrownBy(() ->
                productImageForTests.switchToMain())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This image is already the main.");
    }

    @Test
    @DisplayName("Should successfully switch main to gallery.")
    void shouldSuccessfullySwitchMainToGallery() {
        // Setting the stage
        productImageForTests.switchToMain();

        // Act
        productImageForTests.switchToGallery();

        // Asserts
        assertThat(productImageForTests.getType()).isEqualTo(ImageType.GALLERY);
    }

    @Test
    @DisplayName("Should throw an IllegalState exception when switch from gallery to gallery")
    void shouldThrowIllegalStateExceptionWhenSwitchFromGalleryToGallery() {
        // Act / Assert
        assertThatThrownBy(() ->
                productImageForTests.switchToGallery())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This image is already in the gallery.");
    }

    @Test
    @DisplayName("Should return true when is the main")
    void shouldReturnTrueWhenIsMain() {
        // Setting the stage
        productImageForTests.switchToMain();

        // Assert
        assertThat(productImageForTests.isMain()).isTrue();
    }

    @Test
    @DisplayName("Should return false when isn't the main")
    void shouldReturnFalseWhenIsNotMain() {
        // Assert
        assertThat(productImageForTests.isMain()).isFalse();
    }
}