package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.enums.DimensionUnit;
import br.com.ivogoncalves.catalog_service.domain.enums.ImageType;
import br.com.ivogoncalves.catalog_service.domain.enums.ProductStatus;
import br.com.ivogoncalves.catalog_service.domain.exceptions.DuplicateIdException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.InvalidSkuException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ProductImageLimitExceededException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ResourceNotFoundException;
import br.com.ivogoncalves.catalog_service.infrastructure.persistence.entities.ProductEntity;
import br.com.ivogoncalves.catalog_service.infrastructure.persistence.entities.ProductImageEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * @author Ivo Gonçalves
 */
public class ProductTest {

    private final String path = "https://storage.com/images/";

    private BrandId brandId;
    private CategoryId categoryId;
    private Dimensions dimensions;
    private Product productForTests;
    private final List<ProductImage> productImagesForTests = new ArrayList<>();
    private final List<ProductImageEntity> productImagesEntityForTests = new ArrayList<>();

    @BeforeEach
    void setup() {
        brandId = new BrandId(UUID.randomUUID());
        categoryId = new CategoryId(UUID.randomUUID());

        dimensions = new Dimensions(
                new BigDecimal("38.0"),
                new BigDecimal("26.04"),
                new BigDecimal("2.375"),
                DimensionUnit.CM
        );

        productForTests = Product.create(
                "Notebook",
                "DELL Inspiron 5558",
                new BigDecimal("3500.00"),
                new BigDecimal("3.20"),
                dimensions,
                brandId,
                categoryId
        );

        for (int i = 0; i < 4; i++) {
            productImagesForTests.add(ProductImage.create(
                    path + "photo-" + i + ".png",
                    i
            ));
        }
    }


    // PRODUCT CREATION TESTS
    @Test
    @DisplayName("Should create product successfully with valid data.")
    void shouldCreateProductSuccessfully() {
        Product product = Product.create(
                "      Notebook     ",
                "  Dell Inspiron 5558 laptop   ",
                new BigDecimal("3500.0"),
                new BigDecimal("3.20"),
                dimensions,
                brandId,
                categoryId
        );

        assertThat(product.getId()).isNotNull();
        assertThat(product.getSku()).isNotBlank();
        assertThat(product.getSku()).matches("^sku-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-commerce-flow$");
        assertThat(product.getName()).isEqualTo("Notebook");
        assertThat(product.getDescription()).isEqualTo("Dell Inspiron 5558 laptop");
        assertThat(product.getBasePrice()).isEqualByComparingTo("3500.0");
        assertThat(product.getWeight()).isEqualByComparingTo("3.20");
        assertThat(product.getDimensions().calculateVolume()).isEqualByComparingTo("2350.110");
        assertThat(product.getDimensions().unit()).isEqualTo(DimensionUnit.CM);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getBrandId()).isEqualTo(brandId);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getImages()).isEmpty();
        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when name is null.")
    void shouldThrowIllegalArgumentExceptionWhenNameIsNull() {
        assertThatThrownBy(() ->
                Product.create(null,
                        "Dell Inspiron 5558 laptop",
                        new BigDecimal("3500.0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when name is empty.")
    void shouldThrowIllegalArgumentExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() ->
                Product.create("",
                        "Dell Inspiron 5558 laptop",
                        new BigDecimal("3500.0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when name is blank.")
    void shouldThrowIllegalArgumentExceptionWhenNameIsBlank() {
        assertThatThrownBy(() ->
                Product.create("     ",
                        "Dell Inspiron 5558 laptop",
                        new BigDecimal("3500.0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name required.");
    }


    @Test
    @DisplayName("Should throw an IllegalArgument exception when description is null.")
    void shouldThrowIllegalArgumentExceptionWhenDescriptionIsNull() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        null,
                        new BigDecimal("3500.0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when description is empty.")
    void shouldThrowIllegalArgumentExceptionWhenDescriptionIsEmpty() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "",
                        new BigDecimal("3500.0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when description is blank.")
    void shouldThrowIllegalArgumentExceptionWhenDescriptionIsBlank() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "    ",
                        new BigDecimal("3500.0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when price is null.")
    void shouldThrowIllegalArgumentExceptionWhenPriceIsNull() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        null,
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when price is zero.")
    void shouldThrowIllegalArgumentExceptionWhenPriceIsZero() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value must be greater than zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when price is less than zero.")
    void shouldThrowIllegalArgumentExceptionWhenPriceIsLessThanZero() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("-3500.0"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value must be greater than zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when weight is null.")
    void shouldThrowIllegalArgumentExceptionWhenWeightIsNull() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("3500.00"),
                        null,
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight value cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when weight is zero.")
    void shouldThrowIllegalArgumentExceptionWhenWeightIsZero() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("3500.00"),
                        new BigDecimal("0"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight of a product cannot be negative or equal to zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when weight is less than zero.")
    void shouldThrowIllegalArgumentExceptionWhenWeightIsLessThanZero() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("3500.00"),
                        new BigDecimal("-3.20"),
                        dimensions,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight of a product cannot be negative or equal to zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when dimensions is null.")
    void shouldThrowIllegalArgumentExceptionWhenDimensionsIsNull() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("3500.00"),
                        new BigDecimal("3.20"),
                        null,
                        brandId,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dimensions cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when brand id is null.")
    void shouldThrowIllegalArgumentExceptionWhenBrandIdIsNull() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("3500.00"),
                        new BigDecimal("3.20"),
                        dimensions,
                        null,
                        categoryId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Brand Id cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when category id is null.")
    void shouldThrowIllegalArgumentExceptionWhenCategoryIdIsNull() {
        assertThatThrownBy(() ->
                Product.create("Notebook",
                        "DELL Inspiron 5558",
                        new BigDecimal("3500.00"),
                        new BigDecimal("3.20"),
                        dimensions,
                        brandId,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category Id cannot be null.");
    }

    // TESTING THE IMMUTABILITY OF THE LIST
    @Test
    @DisplayName("Should throw an UnsupportedOperation exception when trying to modify the images list.")
    void shouldThrowUnsupportedOperationExceptionWhenTryingToModifyImagesList() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));

        var images  = productForTests.getImages();

        // Assert for all them
        assertThat(productForTests.getImages().size()).isEqualTo(3);
        assertThat(productForTests.getUpdatedAt()).isNotNull();


        assertThatThrownBy(() ->
                images.add(ProductImage.create("https://storage.com/images/photo-3.png", 3))
        )
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() ->
                images.remove(2)
        )
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // PRODUCT UPDATE TESTS
    @Test
    @DisplayName("Should successfully update the product name.")
    void shouldSuccessfullyUpdateProductName() {
        // Act
        productForTests.updateName("   Notebook DELL   ");

        // Assert
        assertThat(productForTests.getName()).isEqualTo("Notebook DELL");
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product name with null value")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNameWithNullValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product name with empty value")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNameWithEmptyValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product name with blank value")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNameWithBlankValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the new product name to be the same as the old name")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNewNameSameOldName() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateName("  Notebook "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("It is not permitted to update the new product name to the same value as the old name.");
    }

    @Test
    @DisplayName("Should successfully update the product description.")
    void shouldSuccessfullyUpdateProductDescription() {
        // Act
        productForTests.updateDescription("    High Performance laptop   ");

        // Assert
        assertThat(productForTests.getDescription()).isEqualTo("High Performance laptop");
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product description with null value")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingDescriptionWithNullValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateDescription(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product description with empty value")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingDescriptionWithEmptyValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateDescription(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product description with blank value")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingDescriptionWithBlankValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateDescription("    "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description required.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the new product description to be the same as the old description")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNewDescriptionSameOldDescription() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateDescription("  DELL Inspiron 5558 "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("It is not permitted to update the new product description to the same value as the old description.");
    }

    @Test
    @DisplayName("Should successfully update the product price.")
    void shouldSuccessfullyUpdateProductPrice() {
        // Act
        productForTests.updatePrice(new BigDecimal("4200.12"));

        // Assert
        assertThat(productForTests.getBasePrice()).isEqualByComparingTo("4200.12");
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product value with null value")
    void shouldThrowIllegalArgumentExceptionWhenUpdatePriceWithNullValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updatePrice(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product value is zero")
    void shouldThrowIllegalArgumentExceptionWhenUpdatePriceWithIsZero() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updatePrice(new BigDecimal("0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value must be greater than zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product value is less than zero")
    void shouldThrowIllegalArgumentExceptionWhenUpdatePriceWithIsLessThanZero() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updatePrice(new BigDecimal("-3500.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value must be greater than zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the new product price to be the same as the old price")
    void shouldThrowIllegalArgumentExceptionWhenUpdateNewPriceSameOldPrice() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updatePrice(new BigDecimal("3500.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("It is not permitted to update the new price to the same value as the old price.");
    }

    @Test
    @DisplayName("Should successfully update the product weight.")
    void shouldSuccessfullyUpdateProductWeight() {
        // Act
        productForTests.updateWeight(new BigDecimal("2.90"));

        // Assert
        assertThat(productForTests.getWeight()).isEqualByComparingTo("2.90");
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product weight with null value")
    void shouldThrowIllegalArgumentExceptionWhenUpdateWeightWithNullValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateWeight(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight value cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product weight is zero")
    void shouldThrowIllegalArgumentExceptionWhenUpdateWeightIsZero() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateWeight(new BigDecimal("0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight of a product cannot be negative or equal to zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product weight is less than zero")
    void shouldThrowIllegalArgumentExceptionWhenUpdateWeightIsLessThanZero() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateWeight(new BigDecimal("-3.20")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight of a product cannot be negative or equal to zero.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the new product weight to be the same as the old weight")
    void shouldThrowIllegalArgumentExceptionWhenUpdateNewWeightSameOldWeight() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateWeight(new BigDecimal("3.20")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("It is not permitted to update the new weight to the same value as the old weight.");
    }

    @Test
    @DisplayName("Should successfully update the product dimensions.")
    void shouldSuccessfullyUpdateProductDimension() {
        Dimensions dimensionsForUpdate = new Dimensions(
                new BigDecimal("42.00"),
                new BigDecimal("29.04"),
                new BigDecimal("1.975"),
                DimensionUnit.CM
        );

        // Act
        productForTests.updateDimensions(dimensionsForUpdate);

        // Assert
        assertThat(productForTests.getDimensions()).isEqualTo(dimensionsForUpdate);
        assertThat(productForTests.getDimensions().calculateVolume()).isEqualByComparingTo("2408.868");
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the product dimensions with null value")
    void shouldThrowIllegalArgumentExceptionWhenUpdateDimensionWithNullValue() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.updateDimensions(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dimensions cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgument exception when updating the new product dimensions to be the same as the old dimensions")
    void shouldThrowIllegalArgumentExceptionWhenUpdateNewDimensionsSameOldDimensions() {
        Dimensions dimensionsForUpdate = new Dimensions(
                new BigDecimal("38.0"),
                new BigDecimal("26.04"),
                new BigDecimal("2.375"),
                DimensionUnit.CM
        );

        assertThatThrownBy(() -> productForTests.updateDimensions(dimensionsForUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("It is not permitted to update the new dimensions to the same value as the old dimensions.");
    }

    // PRODUCT STATUS TESTS
    @Test
    @DisplayName("Should return true when status is active")
    void shouldReturnTrueWhenStatusIsActive() {
        // Act / Assert
        assertThat(productForTests.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return false when status is inactive")
    void shouldReturnFalseWhenStatusIsInactive() {
        // Setting the stage
        productForTests.deactivate();
        // Act / Assert
        assertThat(productForTests.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should successfully activate the product status.")
    void shouldSuccessfullyActiveProductStatus() {
        // Setting the stage
        productForTests.deactivate();

        // Act
        productForTests.activate();

        // Assert
        assertThat(productForTests.isActive()).isTrue();
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }


    @Test
    @DisplayName("Should successfully discontinue the product status.")
    void shouldSuccessfullyActiveProductStatusWhenProductStatusWhenProductStatusIsDiscontinued() {
        // Act
        productForTests.discontinue();

        // Assert
        assertThat(productForTests.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should successfully activate a discontinued product.")
    void shouldSuccessfullyActivateDiscontinuedProduct() {
        // Setting the stage
        productForTests.discontinue();

        // Act
        productForTests.activate();

        // Assert
        assertThat(productForTests.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(productForTests.isActive()).isTrue();
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw IllegalStateException when product is already discontinued")
    void shouldThrowIllegalArgumentExceptionWhenProductIsAlreadyDiscontinued() {
        // Setting the stage
        productForTests.discontinue();

        // Act
        assertThatThrownBy(() -> productForTests.discontinue())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Product is already discontinued.");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attempting to activate a product that is already active")
    void shouldThrowIllegalArgumentExceptionWhenToActivateProductAlreadyActive() {
        // Act / Assert
        assertThatThrownBy(() -> productForTests.activate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The product is already active.");
    }

    @Test
    @DisplayName("Should successfully deactivate the product status")
    void shouldSuccessfullyDeactivateProductStatus() {
        // Act
        productForTests.deactivate();

        // Assert
        assertThat(productForTests.isActive()).isFalse();
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attempting to deactivate a product that is already inactive")
    void shouldThrowIllegalArgumentExceptionWhenToDeactivateProductAlreadyInactive() {
        // Setting the stage
        productForTests.deactivate();

        assertThatThrownBy(() -> productForTests.deactivate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The product is already inactive.");
    }

    // PRODUCT IMAGES TESTS
    @Test
    @DisplayName("Should successfully add the first product image.")
    void shouldSuccessfullyAddFirstProductImage() {
        // Act
        productForTests.addImage(productImagesForTests.getFirst());

        // Assert
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().size()).isEqualTo(1);
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should successfully add the first product image, regardless of the order.")
    void shouldSuccessfullyAddFirstProductImageRegardlessOrder() {
        // Act
        productForTests.addImage(productImagesForTests.get(3));

        // Assert
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().size()).isEqualTo(1);
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();
        assertThat(productForTests.getCreatedAt()).isNotNull();
        assertThat(productForTests.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should successfully add the second product image.")
    void shouldSuccessfullyAddSecondProductImage() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());

        // Act
        productForTests.addImage(productImagesForTests.get(1));

        // Assert for both
        assertThat(productForTests.getImages().size()).isEqualTo(2);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();
    }

    @Test
    @DisplayName("The four product images should be added successfully.")
    void shouldSuccessfullyAddFourProductImage() {
        // Act
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Assert for all them
        assertThat(productForTests.getImages().size()).isEqualTo(4);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(2).isMain()).isFalse();

        // Assert for fourth image
        assertThat(productForTests.getImages().get(3).getId()).isNotNull();
        assertThat(productForTests.getImages().get(3).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(3).getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().get(3).getOrder()).isEqualTo(3);
        assertThat(productForTests.getImages().get(3).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(3).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when adding an image that already exists.")
    void shouldThrowIllegalArgumentExceptionWhenAddingImageAlreadyExists() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.addImage(productImagesForTests.getFirst())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An image with this ID already exists.");
    }

    @Test
    @DisplayName("Should throw a ProductImageLimitExceededException when an image is added beyond the permitted limit.")
    void shouldThrowProductImageLimitExceededExceptionWhenAddImageExceedsLimit() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Creating an image for test
        var image = ProductImage.create(path + "photo-04.png", 3);

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.addImage(image)
        ).isInstanceOf(ProductImageLimitExceededException.class)
                .hasMessage("The allowed number of images per product has already reached the limit. Limit: 4");

        assertThat(productForTests.getImages().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when setting an image as 'main' if that image is already the 'main' one.")
    void shouldThrowIllegalArgumentExceptionWhenSetImageMainImageAlreadyMain() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));


        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.setMainImage(productForTests.getImages().getFirst().getId())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This image is already main");

        assertThat(productForTests.getImages().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when add image with null value")
    void shouldThrowIllegalArgumentExceptionWhenAddWithNullValue() {
        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.addImage(null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The product image cannot be null.");
    }

    @Test
    @DisplayName("Should successfully change the main image.")
    void shouldSuccessfullyChangeMainImage() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act
        productForTests.setMainImage(productForTests.getImages().get(2).getId());

        // Assert for the all them
        assertThat(productForTests.getImages().size()).isEqualTo(4);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().getFirst().isMain()).isFalse();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().get(2).isMain()).isTrue();

        // Assert for fourth image
        assertThat(productForTests.getImages().get(3).getId()).isNotNull();
        assertThat(productForTests.getImages().get(3).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(3).getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().get(3).getOrder()).isEqualTo(3);
        assertThat(productForTests.getImages().get(3).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(3).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when change the main image with product image id is null.")
    void shouldThrowIllegalArgumentExceptionWhenChangeMainImageProductImageIdIsNull() {
        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.setMainImage(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The provided image ID cannot be null.");
    }

    @Test
    @DisplayName("Should throw a ResourceNotFoundException when change the main image product image id is not found.")
    void shouldThrowResourceNotFoundExceptionWhenChangeMainImageWithProductImageIdNotFound() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        UUID idRandomForTest = UuidCreator.getTimeOrderedEpoch();

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.setMainImage(new ProductImageId(idRandomForTest))
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't records for this id.");
    }

    @Test
    @DisplayName("Should successfully remove an image of the main type")
    void shouldSuccessfullyRemoveImageOfMainType() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act
        productForTests.removeImage(productForTests.getImages().getFirst().getId());

        // Assert for the all them
        assertThat(productForTests.getImages().size()).isEqualTo(3);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(2).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should successfully remove an image of the gallery type")
    void shouldSuccessfullyRemoveImageOfGalleryType() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act
        productForTests.removeImage(productForTests.getImages().get(1).getId());

        // Assert for the all them
        assertThat(productForTests.getImages().size()).isEqualTo(3);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(2).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when the product image ID to be removed is null.")
    void shouldThrowIllegalArgumentExceptionWhenProductImageIdIsNull() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.removeImage(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The image id cannot be null.");
    }

    @Test
    @DisplayName("Should throw an ResourceNotFoundException when the product image ID to be removed is null.")
    void shouldThrowResourceNotFoundExceptionWhenProductImageIdNotFound() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        UUID idForTest = UuidCreator.getTimeOrderedEpoch();

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.removeImage(new ProductImageId(idForTest))
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't records for this id.");
    }

    @Test
    @DisplayName("Should successfully update the product image order.")
    void shouldSuccessfullyUpdateProductImageOrder() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act
        productForTests.updateImageOrder(productImagesForTests.get(1).getId(), 3);

        // Assert for the all them
        assertThat(productForTests.getImages().size()).isEqualTo(4);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(2).isMain()).isFalse();

        // Assert for fourth image
        assertThat(productForTests.getImages().get(3).getId()).isNotNull();
        assertThat(productForTests.getImages().get(3).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(3).getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().get(3).getOrder()).isEqualTo(3);
        assertThat(productForTests.getImages().get(3).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(3).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should successfully update the order from the first image.")
    void shouldSuccessfullyUpdateOrderFromFirstImage() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act
        productForTests.updateImageOrder(productImagesForTests.getFirst().getId(), 2);

        // Assert for the all them
        assertThat(productForTests.getImages().size()).isEqualTo(4);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().getFirst().isMain()).isFalse();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().get(2).isMain()).isTrue();

        // Assert for fourth image
        assertThat(productForTests.getImages().get(3).getId()).isNotNull();
        assertThat(productForTests.getImages().get(3).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(3).getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().get(3).getOrder()).isEqualTo(3);
        assertThat(productForTests.getImages().get(3).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(3).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should successfully update the order from the last image.")
    void shouldSuccessfullyUpdateOrderFromLastImage() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act
        productForTests.updateImageOrder(productImagesForTests.get(3).getId(), 0);

        // Assert for the all them
        assertThat(productForTests.getImages().size()).isEqualTo(4);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().getFirst().isMain()).isFalse();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().get(1).isMain()).isTrue();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(2).isMain()).isFalse();

        // Assert for fourth image
        assertThat(productForTests.getImages().get(3).getId()).isNotNull();
        assertThat(productForTests.getImages().get(3).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(3).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(3).getOrder()).isEqualTo(3);
        assertThat(productForTests.getImages().get(3).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(3).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should successfully update the order from the middle image.")
    void shouldSuccessfullyUpdateOrderFromMiddleImage() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act
        productForTests.updateImageOrder(productImagesForTests.get(2).getId(), 1);

        // Assert for the all them
        assertThat(productForTests.getImages().size()).isEqualTo(4);
        assertThat(productForTests.getUpdatedAt()).isNotNull();

        // Assert for first image
        assertThat(productForTests.getImages().getFirst().getId()).isNotNull();
        assertThat(productForTests.getImages().getFirst().getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().getFirst().getUrl()).isEqualTo(path + "photo-0.png");
        assertThat(productForTests.getImages().getFirst().getOrder()).isEqualTo(0);
        assertThat(productForTests.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(productForTests.getImages().getFirst().isMain()).isTrue();

        // Assert for second image
        assertThat(productForTests.getImages().get(1).getId()).isNotNull();
        assertThat(productForTests.getImages().get(1).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(1).getUrl()).isEqualTo(path + "photo-2.png");
        assertThat(productForTests.getImages().get(1).getOrder()).isEqualTo(1);
        assertThat(productForTests.getImages().get(1).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(1).isMain()).isFalse();

        // Assert for third image
        assertThat(productForTests.getImages().get(2).getId()).isNotNull();
        assertThat(productForTests.getImages().get(2).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(2).getUrl()).isEqualTo(path + "photo-1.png");
        assertThat(productForTests.getImages().get(2).getOrder()).isEqualTo(2);
        assertThat(productForTests.getImages().get(2).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(2).isMain()).isFalse();

        // Assert for fourth image
        assertThat(productForTests.getImages().get(3).getId()).isNotNull();
        assertThat(productForTests.getImages().get(3).getId()).isInstanceOf(ProductImageId.class);
        assertThat(productForTests.getImages().get(3).getUrl()).isEqualTo(path + "photo-3.png");
        assertThat(productForTests.getImages().get(3).getOrder()).isEqualTo(3);
        assertThat(productForTests.getImages().get(3).getType()).isEqualTo(ImageType.GALLERY);
        assertThat(productForTests.getImages().get(3).isMain()).isFalse();
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when changing to the same order.")
    void shouldThrowIllegalArgumentExceptionWhenChangingSameOrder() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act / Assert
        assertThatThrownBy(() ->
                    productForTests.updateImageOrder(productForTests.getImages().get(1).getId(), 1)
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating the new order to be identical to the old order is not permitted.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when updating order of images if the product image id is null")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingOrderWithProductImageIdIsNull() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.updateImageOrder(null, 2)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The provided image ID or new order cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when updating order of images if the new order is null")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingOrderWithNewOrderIsNull() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.updateImageOrder(productForTests.getImages().get(1).getId(), null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The provided image ID or new order cannot be null.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when updating order of images if the product image id not found.")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingOrderWithProductImageIdNotFound() {
        // Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        UUID idForTest = UuidCreator.getTimeOrderedEpoch();

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.updateImageOrder(new ProductImageId(idForTest), 2)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There aren't records for this id.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when updating order of images if the new order greater than the limit list")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingOrderIfNewOrderGreaterThanLimitList() {
        //Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.updateImageOrder(productForTests.getImages().get(1).getId(), 4)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The new order cannot be greater than the limit of the list or less than 0.");
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when updating order of images if the new order less than 0.")
    void shouldThrowIllegalArgumentExceptionWhenUpdatingOrderIfNewOrderLessThanZero() {
        //Setting the stage
        productForTests.addImage(productImagesForTests.getFirst());
        productForTests.addImage(productImagesForTests.get(1));
        productForTests.addImage(productImagesForTests.get(2));
        productForTests.addImage(productImagesForTests.get(3));

        // Act / Assert
        assertThatThrownBy(() ->
                productForTests.updateImageOrder(productForTests.getImages().get(1).getId(), -1)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The new order cannot be greater than the limit of the list or less than 0.");
    }

    // RECONSTRUCTING PRODUCT TESTS
    @Test
    @DisplayName("Should successfully reconstructing the product the with data validation")
    void shouldSuccessfullyReconstructingProduct() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }


        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );



        // Act
        var product = Product.reconstruction(
                new ProductId(productEntity.getId()),
                productEntity.getSku(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getBasePrice(),
                productEntity.getWeight(),
                new Dimensions(
                        productEntity.getDimensionLength(),
                        productEntity.getDimensionWidth(),
                        productEntity.getDimensionHeight(),
                        productEntity.getDimensionUnit()
                ),
                new BrandId(productEntity.getBrandId()),
                new CategoryId(productEntity.getCategoryId()),
                productEntity.getStatus(),
                new ArrayList<>(
                        List.of(
                                ProductImage.reconstituite(
                                        productEntity.getImages().getFirst().getId(),
                                        productEntity.getImages().getFirst().getUrl(),
                                        productEntity.getImages().getFirst().getType(),
                                        productEntity.getImages().getFirst().getImageOrder()
                                ),
                                ProductImage.reconstituite(
                                        productEntity.getImages().get(1).getId(),
                                        productEntity.getImages().get(1).getUrl(),
                                        productEntity.getImages().get(1).getType(),
                                        productEntity.getImages().get(1).getImageOrder()
                                ),
                                ProductImage.reconstituite(
                                        productEntity.getImages().get(2).getId(),
                                        productEntity.getImages().get(2).getUrl(),
                                        productEntity.getImages().get(2).getType(),
                                        productEntity.getImages().get(2).getImageOrder()
                                ),
                                ProductImage.reconstituite(
                                        productEntity.getImages().get(3).getId(),
                                        productEntity.getImages().get(3).getUrl(),
                                        productEntity.getImages().get(3).getType(),
                                        productEntity.getImages().get(3).getImageOrder()
                                )
                        )
                ),
                productEntity.getCreatedAt(),
                productEntity.getUpdatedAt()
        );

        // Assert
        assertThat(product.getId().id()).isEqualTo(productEntity.getId());
        assertThat(product.getSku()).isNotBlank();
        assertThat(product.getSku()).isEqualTo(productEntity.getSku());
        assertThat(product.getName()).isEqualTo("Multimedia Keyboard");
        assertThat(product.getDescription()).isEqualTo("Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.");
        assertThat(product.getBasePrice()).isEqualByComparingTo("87.00");
        assertThat(product.getWeight()).isEqualByComparingTo("0.700");
        assertThat(product.getDimensions().calculateVolume()).isEqualByComparingTo("2292.300");
        assertThat(product.getDimensions().unit()).isEqualTo(DimensionUnit.CM);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getBrandId()).isEqualTo(brandId);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getImages()).hasSize(4).extracting(ProductImage::getOrder).containsExactlyInAnyOrder(0, 1, 2, 3);
        assertThat(product.getImages()).filteredOn(ProductImage::isMain).hasSize(1);
        assertThat(product.getImages().getFirst().getType()).isEqualTo(ImageType.MAIN);
        assertThat(product.getCreatedAt()).isEqualTo(productEntity.getCreatedAt());
        assertThat(product.getUpdatedAt()).isEqualTo(productEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null id value")
    void shouldThrowExceptionWhenReconstructingProductWithNullId() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        null,
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("Product ID cannot be null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null skull value")
    void shouldThrowExceptionWhenReconstructingProductWithNullSku() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        null,
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(InvalidSkuException.class)
                .hasMessage("The SKU of this product is non-standard or null or empty " +
                        "Pattern: sku-5f884b23-db14-4e6b-a1de-b4d171e4d8fc-commerce-flow");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null name value")
    void shouldThrowExceptionWhenReconstructingProductWithNullName() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        null,
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name required.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null description value")
    void shouldThrowExceptionWhenReconstructingProductWithNullDescription() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        null,
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description required.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null price value")
    void shouldThrowExceptionWhenReconstructingProductWithNullPrice() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        null,
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value cannot be null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with price is equal zero.")
    void shouldThrowExceptionWhenReconstructingProductWithPriceIsEqualZero() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        new BigDecimal("0"),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The price value must be greater than zero.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null weight value")
    void shouldThrowExceptionWhenReconstructingProductWithNullWeight() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        null,
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight value cannot be null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with weight is equal zero.")
    void shouldThrowExceptionWhenReconstructingProductWithWeightIsEqualZero() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        new BigDecimal("0"),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The weight of a product cannot be negative or equal to zero.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null dimensions value.")
    void shouldThrowExceptionWhenReconstructingProductWithNullDimensions() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        null,
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dimensions cannot be null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null brandId value.")
    void shouldThrowExceptionWhenReconstructingProductWithNullBrandId() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        null,
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Brand Id cannot be null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null categoryId value.")
    void shouldThrowExceptionWhenReconstructingProductWithNullCategoryId() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        null,
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category Id cannot be null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null status value.")
    void shouldThrowExceptionWhenReconstructingProductWithNullStatus() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        null,
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("Product status cannot be null.");
    }

    @Test
    @DisplayName("Should successfully reconstructing the product with empty images list")
    void shouldThrowExceptionWhenReconstructingProductWithEmptyImagesList() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act
        var product = Product.reconstruction(
                new ProductId(productEntity.getId()),
                productEntity.getSku(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getBasePrice(),
                productEntity.getWeight(),
                new Dimensions(
                        productEntity.getDimensionLength(),
                        productEntity.getDimensionWidth(),
                        productEntity.getDimensionHeight(),
                        productEntity.getDimensionUnit()
                ),
                new BrandId(productEntity.getBrandId()),
                new CategoryId(productEntity.getCategoryId()),
                productEntity.getStatus(),
                new ArrayList<>(),
                productEntity.getCreatedAt(),
                productEntity.getUpdatedAt()
        );

        // Assert
        assertThat(product.getId()).isNotNull();
        assertThat(product.getSku()).isNotBlank();
        assertThat(product.getSku()).matches("^sku-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-commerce-flow$");
        assertThat(product.getName()).isEqualTo("Multimedia Keyboard");
        assertThat(product.getDescription()).isEqualTo("Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.");
        assertThat(product.getBasePrice()).isEqualByComparingTo("87.00");
        assertThat(product.getWeight()).isEqualByComparingTo("0.700");
        assertThat(product.getDimensions().calculateVolume()).isEqualByComparingTo("2292.300");
        assertThat(product.getDimensions().unit()).isEqualTo(DimensionUnit.CM);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getBrandId()).isEqualTo(brandId);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getImages()).isEmpty();
        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an IllegalArgumentException when reconstructing the product with a list of images containing elements with duplicate ID.")
    void showThrowExceptionWhenReconstructingProductWithListImagesContainingElementsDuplicateId() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();
        UUID id = UuidCreator.getTimeOrderedEpoch();


        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
       assertThatThrownBy(() ->
               Product.reconstruction(
                       new ProductId(productEntity.getId()),
                       productEntity.getSku(),
                       productEntity.getName(),
                       productEntity.getDescription(),
                       productEntity.getBasePrice(),
                       productEntity.getWeight(),
                       new Dimensions(
                               productEntity.getDimensionLength(),
                               productEntity.getDimensionWidth(),
                               productEntity.getDimensionHeight(),
                               productEntity.getDimensionUnit()
                       ),
                       new BrandId(productEntity.getBrandId()),
                       new CategoryId(productEntity.getCategoryId()),
                       productEntity.getStatus(),
                       new ArrayList<>(
                               List.of(
                                       ProductImage.reconstituite(
                                               id,
                                               productEntity.getImages().getFirst().getUrl(),
                                               productEntity.getImages().getFirst().getType(),
                                               productEntity.getImages().getFirst().getImageOrder()
                                       ),
                                       ProductImage.reconstituite(
                                               id,
                                               productEntity.getImages().get(1).getUrl(),
                                               productEntity.getImages().get(1).getType(),
                                               productEntity.getImages().get(1).getImageOrder()
                                       ),
                                       ProductImage.reconstituite(
                                               productEntity.getImages().get(2).getId(),
                                               productEntity.getImages().get(2).getUrl(),
                                               productEntity.getImages().get(2).getType(),
                                               productEntity.getImages().get(2).getImageOrder()
                                       ),
                                       ProductImage.reconstituite(
                                               productEntity.getImages().get(3).getId(),
                                               productEntity.getImages().get(3).getUrl(),
                                               productEntity.getImages().get(3).getType(),
                                               productEntity.getImages().get(3).getImageOrder()
                                       )
                               )
                       ),
                       productEntity.getCreatedAt(),
                       productEntity.getUpdatedAt()
               )
       )
               .isInstanceOf(DuplicateIdException.class)
               .hasMessage("The list cannot contain elements with duplicate IDs. Duplicate ID: "  + id);
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with null images value.")
    void shouldThrowExceptionWhenReconstructingProductWithNullProductImages() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        null,
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("Images cannot be null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with a list of images exceeding the limit.")
    void shouldThrowExceptionWhenReconstructingProductWithProductImagesExceedingLimit() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                UuidCreator.getTimeOrderedEpoch(),
                                                path + "photo-" + 4 + ".png",
                                                ImageType.GALLERY,
                                                3
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(ProductImageLimitExceededException.class)
                .hasMessage("The allowed number of images per product has already reached the limit. Limit: 4");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with a list of images containing a null element.")
    void shouldThrowExceptionWhenreconstructingProductWithProductImagesContainingNullElement() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                Arrays.asList(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        null
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product images cannot contain null.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product With a list of images that contains no main image")
    void shouldThrowExceptionWhenReconstructingProductWithProductImagesContainsNoMainImage() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        productEntity.getImages().get(0).setType(ImageType.GALLERY);

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A product with images must have exactly one main image.");
    }

    @Test
    @DisplayName("Should throw exception when reconstructing the product with a list of images containing two main")
    void shouldThrowExceptionWhenReconstructingProductWithProductImagesContainingTwoMain() {
        // Setting the stage
        UUID productEntityId = UuidCreator.getTimeOrderedEpoch();
        UUID skuEntity = UuidCreator.getTimeOrderedEpoch();

        for (int i = 0; i < 4; i++) {
            productImagesEntityForTests.add(new ProductImageEntity(
                    UuidCreator.getTimeOrderedEpoch(),
                    path + "photo-" + i + ".png",
                    i == 0 ? ImageType.MAIN : ImageType.GALLERY,
                    i,
                    productEntityId
            ));

        }

        var productEntity = new ProductEntity(
                productEntityId,
                "sku-" + skuEntity + "-commerce-flow",
                "Multimedia Keyboard",
                "Multimedia keyboard: sound, video, and volume control without taking your hands off the keyboard.",
                new BigDecimal("87.00"),
                new BigDecimal("0.700"),
                new BigDecimal("16.98"),
                new BigDecimal("45.00"),
                new BigDecimal("3.00"),
                DimensionUnit.CM,
                ProductStatus.ACTIVE,
                brandId.id(),
                categoryId.id(),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                productImagesEntityForTests
        );

        productEntity.getImages().get(1).setType(ImageType.MAIN);

        // Act / Assert
        assertThatThrownBy(() ->
                Product.reconstruction(
                        new ProductId(productEntity.getId()),
                        productEntity.getSku(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getBasePrice(),
                        productEntity.getWeight(),
                        new Dimensions(
                                productEntity.getDimensionLength(),
                                productEntity.getDimensionWidth(),
                                productEntity.getDimensionHeight(),
                                productEntity.getDimensionUnit()
                        ),
                        new BrandId(productEntity.getBrandId()),
                        new CategoryId(productEntity.getCategoryId()),
                        productEntity.getStatus(),
                        new ArrayList<>(
                                List.of(
                                        ProductImage.reconstituite(
                                                productEntity.getImages().getFirst().getId(),
                                                productEntity.getImages().getFirst().getUrl(),
                                                productEntity.getImages().getFirst().getType(),
                                                productEntity.getImages().getFirst().getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(1).getId(),
                                                productEntity.getImages().get(1).getUrl(),
                                                productEntity.getImages().get(1).getType(),
                                                productEntity.getImages().get(1).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(2).getId(),
                                                productEntity.getImages().get(2).getUrl(),
                                                productEntity.getImages().get(2).getType(),
                                                productEntity.getImages().get(2).getImageOrder()
                                        ),
                                        ProductImage.reconstituite(
                                                productEntity.getImages().get(3).getId(),
                                                productEntity.getImages().get(3).getUrl(),
                                                productEntity.getImages().get(3).getType(),
                                                productEntity.getImages().get(3).getImageOrder()
                                        )
                                )
                        ),
                        productEntity.getCreatedAt(),
                        productEntity.getUpdatedAt()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A product with images must have exactly one main image.");
    }
}