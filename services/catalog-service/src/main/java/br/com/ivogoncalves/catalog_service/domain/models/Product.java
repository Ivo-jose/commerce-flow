package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.enums.ProductStatus;
import br.com.ivogoncalves.catalog_service.domain.exceptions.DuplicateIdException;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        this.description = description.trim();
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

    private Product(
            ProductId id,
            String sku,
            String name,
            String description,
            BigDecimal basePrice,
            BigDecimal weight,
            Dimensions dimensions,
            BrandId brandId,
            CategoryId categoryId,
            ProductStatus status,
            List<ProductImage> images,
            Instant createdAt,
            Instant updatedAt
            ) {

        validateName(name);
        validateSku(sku);
        validateDescription(description);
        validatePrice(basePrice);
        validateWeight(weight);
        validateDimensions(dimensions);
        validateBrand(brandId);
        validateCategory(categoryId);

        this.id =  Objects.requireNonNull(id, "Product ID cannot be null.");
        this.sku = sku;
        this.name = name.trim();
        this.description =  description.trim();
        this.basePrice = basePrice;
        this.weight = weight;
        this.dimensions = dimensions;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.status = Objects.requireNonNull(status, "Product status cannot be null.");
        this.images = Objects.requireNonNull(images, "Images cannot be null.");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation date cannot be null.");
        this.updatedAt = updatedAt;
        validateImages(images);
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

    public static Product reconstitute(
            ProductId id,
            String sku,
            String name,
            String description,
            BigDecimal basePrice,
            BigDecimal weight,
            Dimensions dimensions,
            BrandId brandId,
            CategoryId categoryId,
            ProductStatus status,
            List<ProductImage> images,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Product(id,
                sku,
                name,
                description,
                basePrice,
                weight,
                dimensions,
                brandId,
                categoryId,
                status,
                images,
                createdAt,
                updatedAt);
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

    public void discontinue() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Product is already discontinued.");
        }

        this.status = ProductStatus.DISCONTINUED;
        this.updatedAt = Instant.now();
    }

    public void addImage(ProductImage image) {
        if (image == null)
            throw new IllegalArgumentException("The product image cannot be null.");
        if (this.images.size() >= 4)
            throw new ProductImageLimitExceededException("The allowed number of images per product has already reached the limit. Limit: 4");

        if (this.images.stream().anyMatch(existing -> existing.getId().equals(image.getId()))) {
            throw new IllegalArgumentException("An image with this ID already exists.");
        }

        int order = this.images.size();
        if (order != image.getOrder()) {
            image.updateOrder(order);
        }

        if (this.images.isEmpty()) {
            image.switchToMain();
        }

        this.images.add(image);

        setUpdatedAt();
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
        if (image.isMain()) {
            throw new IllegalArgumentException("This image is already main");
        }
        this.images.stream().filter(ProductImage::isMain).forEach(ProductImage::switchToGallery);
        image.switchToMain();
        setUpdatedAt();
    }

    public void updateImageOrder(ProductImageId imageId, Integer newOrder) {
        if (imageId == null || newOrder == null)
            throw new IllegalArgumentException("The provided image ID or new order cannot be null.");

        if (newOrder > this.images.size() - 1 || newOrder < 0)
            throw new IllegalArgumentException("The new order cannot be greater than the limit of the list or less than 0.");

        var image = this.images.stream().filter(img -> img.getId().equals(imageId)).findFirst().orElseThrow(
                () -> new ResourceNotFoundException("There aren't records for this id."));

        if (Objects.equals(image.getOrder(), newOrder)) {
            throw new IllegalArgumentException("Updating the new order to be identical to the old order is not permitted.");
        }

        int idx  = this.images.indexOf(image) == this.getImages().size() -1 ? this.images.indexOf(image) : this.images.indexOf(image) + 1;

        adjustIndexFromImageList(idx, newOrder);

        image.updateOrder(newOrder);

        this.images.sort(Comparator.comparing(ProductImage::getOrder));
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

        if (!hasMainImage() && !this.images.isEmpty()) {
            this.images.getFirst().switchToMain();
        }

        for (int i  = 0;  i < this.images.size(); i++) {
            if (this.images.get(i).getOrder() != i) {
                this.images.get(i).updateOrder(i);
            }
        }

        setUpdatedAt();
    }

    public List<ProductImage> getImages() {
        return Collections.unmodifiableList(this.images);
    }

    private void validateSku(String sku) {
        if (sku == null || sku.isBlank() || !SKU_REGEX.matcher(sku).matches())
            throw new InvalidSkuException("The SKU of this product is non-standard or null or empty " +
                    "Pattern: sku-5f884b23-db14-4e6b-a1de-b4d171e4d8fc-commerce-flow");
    }

    private void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name required.");
        if (name.trim().equals(this.name))
            throw new IllegalArgumentException("It is not permitted to update the new product name to the same value as the old name.");
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("Description required.");
        if (description.trim().equals(this.description))
            throw new IllegalArgumentException("It is not permitted to update the new product description to the same value as the old description.");
    }

    private void validatePrice(BigDecimal price) {
        if (price == null)
            throw new IllegalArgumentException("The price value cannot be null.");
        if (price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("The price value must be greater than zero.");
        if (price.equals(this.basePrice))
            throw new IllegalArgumentException("It is not permitted to update the new price to the same value as the old price.");
    }

    private void validateWeight(BigDecimal weight) {
        if (weight == null)
            throw new IllegalArgumentException("The weight value cannot be null.");
        if (weight.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("The weight of a product cannot be negative or equal to zero.");
        if (weight.equals(this.weight))
            throw new IllegalArgumentException("It is not permitted to update the new weight to the same value as the old weight.");
    }

    private void validateDimensions(Dimensions dimensions) {
        if (dimensions == null)
            throw new IllegalArgumentException("Dimensions cannot be null.");

        if (this.getDimensions() != null && Objects.equals(dimensions.width(), this.getDimensions().width())
                && Objects.equals(dimensions.length(), this.getDimensions().length()) && Objects.equals(dimensions.height(), this.getDimensions().height())) {
            throw new IllegalArgumentException("It is not permitted to update the new dimensions to the same value as the old dimensions.");
        }
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

    private void validateImages(List<ProductImage> images) {
        if (images.size() > 4) {
            throw new ProductImageLimitExceededException("The allowed number of images per product has already reached the limit. Limit: 4");
        }

        if (images.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Product images cannot contain null.");
        }

        long mainImages = images.stream().filter(ProductImage::isMain).count();

        checkDuplicateId(images);

        if (!images.isEmpty() && mainImages != 1) {
            throw new IllegalArgumentException("A product with images must have exactly one main image.");
        }
    }

    private void adjustIndexFromImageList(int idx, Integer newOrder) {
        if (idx < newOrder) {
            for (int i = idx; i <= newOrder; i++) {
                this.images.get(i).updateOrder(this.images.get(i).getOrder() - 1);
            }
        }
        else {
            for (int i = newOrder; i < idx; i++) {
                this.images.get(i).updateOrder(this.images.get(i).getOrder() + 1);
            }
        }
    }

    private void checkDuplicateId(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        Set<UUID> seenIds = new HashSet<>();

        for (ProductImage image : images) {
            if (image == null || image.getId() == null || image.getId().id() == null) {
                continue;
            }

            UUID id = image.getId().id();
            if (!seenIds.add(id)) {
                throw new DuplicateIdException(
                        "The list cannot contain elements with duplicate IDs. Duplicate ID: " + id
                );
            }
        }
    }
}