package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.enums.ProductStatus;
import br.com.ivogoncalves.catalog_service.domain.exceptions.InvalidSkuException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ProductImageLimitExceededException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ResourceNotFoundException;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 *
 * @author Ivo Gonçalves
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {

    @Getter
    @EqualsAndHashCode.Include
    private ProductId id;
    @Getter
    private String sku;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private BigDecimal basePrice;
    @Getter
    private BigDecimal weight;
    @Getter
    private Dimensions dimensions;
    @Getter
    private BrandId brandId;
    @Getter
    private CategoryId categoryId;
    @Getter
    private ProductStatus status;
    @Getter
    private Instant createdAt;
    @Getter
    private Instant updatedAt;

    private List<ProductImage> images;

    private static final Pattern SKU_REGEX = Pattern.compile(
            "^sku-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-commerce-flow$");

    private Product(String name,
                    String description,
                    BigDecimal basePrice,
                    BigDecimal weight,
                    Dimensions dimensions,
                    BrandId brandId,
                    CategoryId categoryId) {
        validateName(name);
        validateDescription(description);
        validatePrice(basePrice);
        validateWeight(weight);
        validateDimensions(dimensions);
        validateBrand(brandId);
        validateCategory(categoryId);

        this.id = new ProductId();
        this.sku = createSku();
        this.name = name.trim();
        this.description = description;
        this.basePrice = basePrice;
        this.weight = weight;
        this.dimensions = dimensions;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.status = ProductStatus.ACTIVE;
        this.images = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    public static Product create(
            String name,
            String description,
            BigDecimal basePrice,
            BigDecimal weight,
            Dimensions dimensions,
            BrandId brandId,
            CategoryId categoryId) {
        return new Product(name, description, basePrice, weight, dimensions, brandId, categoryId);
    }

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName.trim();
        setUpdatedAt();
    }

    public void updateDescription(String newDescription) {
        validateDescription(newDescription);
        this.description = newDescription.trim();
        setUpdatedAt();
    }

    public void updatePrice(BigDecimal newPrice) {
        validatePrice(newPrice);
        this.basePrice = newPrice;
        setUpdatedAt();
    }

    public void updateWeight(BigDecimal newWeight) {
        validateWeight(newWeight);
        this.weight = newWeight;
        setUpdatedAt();
    }

    public void updateDimensions(Dimensions newDimensions) {
        validateDimensions(newDimensions);
        this.dimensions = newDimensions;
        setUpdatedAt();
    }

    public void activate() {
        if (this.status == ProductStatus.ACTIVE)
            throw new IllegalArgumentException("The product is already active.");
        this.status = ProductStatus.ACTIVE;
        setUpdatedAt();
    }

    public void deactivate() {
        if (this.status == ProductStatus.INACTIVE)
            throw new IllegalArgumentException("The product is already inactive.");
        this.status = ProductStatus.INACTIVE;
        setUpdatedAt();
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVE;
    }

    private void validateSku(String sku) {
        if (sku == null || sku.isBlank() || !SKU_REGEX.matcher(sku).matches())
            throw new InvalidSkuException("The SKU of this product is non-standard\n" +
                    "Pattern: sku-5f884b23-db14-4e6b-a1de-b4d171e4d8fc-commerce-flow");
    }

    public void setMainImage(ProductImageId imageId) {
        if (imageId == null)
            throw new IllegalArgumentException("The provided image ID cannot be null.");
        var image = this.images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "There aren't records for this id."));
        this.images.forEach(ProductImage::switchToGallery);
        image.switchToMain();
        setUpdatedAt();
    }

    public void updateImageOrder(ProductImageId imageId, Integer newOrder) {
        if (imageId == null)
            throw new IllegalArgumentException("The provided image ID cannot be null.");
        var image = this.images.stream().filter(img -> img.getId().equals(imageId)).findFirst().orElseThrow(
                () -> new ResourceNotFoundException("There aren't records for this id."));
        image.updateOrder(newOrder);
        setUpdatedAt();
    }

    public void addImage(ProductImage image) {
        if (image == null)
            throw new IllegalArgumentException("The product image cannot be null.");
        if (this.images.size() >= 4)
            throw new ProductImageLimitExceededException("The allowed number of images per product has already reached the limit. Limit: 4");
        if (this.images.isEmpty()) {
            this.images.add(image);
            setMainImage(image.getId());
            return;
        }
        this.images.add(image);
        setUpdatedAt();
    }

    public void removeImage(ProductImageId id) {
        if (id == null)
            throw new IllegalArgumentException("The image id cannot be null.");
        var image = this.images.stream()
                .filter(img -> img.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("There aren't records for this id."));
        if (image.isMain())
            image.switchToGallery();
        this.images.remove(image);
        if (!hasMainImage() && !this.images.isEmpty())
            this.images.getFirst().switchToMain();
        setUpdatedAt();
    }

    public List<ProductImage> getImages() {
        return Collections.unmodifiableList(this.images);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name required.");
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("Updating the description with a null value is not allowed.");
    }

    private void validatePrice(BigDecimal price) {
        if (price == null)
            throw new IllegalArgumentException("The price value cannot be null.");
        if (price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("The price value must be greater than zero.");
    }

    private void validateWeight(BigDecimal weight) {
        if (weight == null)
            throw new IllegalArgumentException("The weight value cannot be null.");
        if (weight.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("The weight of a product cannot be negative or equal to zero.");
    }

    private void validateDimensions(Dimensions dimensions) {
        if (dimensions == null)
            throw new IllegalArgumentException("Dimensions cannot be null.");
    }

    private void validateBrand(BrandId brandId) {
        if (brandId == null)
            throw new IllegalArgumentException("Brand Id cannot be null.");
    }

    private void validateCategory(CategoryId categoryId) {
        if (categoryId == null)
            throw new IllegalArgumentException("Category Id cannot be null.");
    }

    private String createSku() {
        UUID sku_middle = UuidCreator.getTimeOrderedEpoch();
        String completedSku = "sku-" + sku_middle + "-commerce-flow";
        validateSku(completedSku);
        return completedSku;
    }

    private void setUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    private boolean hasMainImage() {
        return this.images.stream()
                .anyMatch(ProductImage::isMain);
    }
}