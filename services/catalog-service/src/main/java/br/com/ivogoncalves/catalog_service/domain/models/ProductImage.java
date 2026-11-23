package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.enums.ImageType;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ProductImageLimitExceededException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Ivo Gonçalves
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductImage {

    @Getter
    @EqualsAndHashCode.Include
    private ProductImageId id;
    @Getter
    private String url;
    @Getter
    private ImageType type;
    @Getter
    private Integer order;

    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(
            "^(https?://)" +
                    "(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+" +
                    "[a-zA-Z]{2,63}|" +
                    "localhost|" +
                    "([0-9]{1,3}\\.){3}[0-9]{1,3})" +
                    "(:[0-9]{1,5})?" +
                    "(/[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)" +
                    "\\.(jpg|jpeg|png|gif|bmp|webp|svg)$");


    private ProductImage(String url, Integer order) {
        validateUrl(url);
        validateOrder(order);
        this.id = new ProductImageId();
        this.url = url.trim();
        this.type = ImageType.GALLERY;
        this.order = order;
    }

    private ProductImage(UUID id, String url, ImageType type, Integer order) {
        validateUrl(url);
        validateOrder(order);
        this.id = new ProductImageId(id);
        this.url = url.trim();
        this.type = type;
        this.order = order;
    }

    public static ProductImage create(String url, Integer order) {
        return new ProductImage(url, order);
    }

    public static ProductImage reconstituite(UUID id, String url, ImageType type, Integer order) {
        return new ProductImage(id, url, type,  order);
    }

     void switchToGallery() {
        if (this.type == ImageType.GALLERY) {
            throw new IllegalStateException("This image is already in the gallery.");
        }
        this.type = ImageType.GALLERY;
    }

     void switchToMain() {
        if (this.type == ImageType.MAIN) {
            throw new IllegalStateException("This image is already the main.");
        }
        this.type = ImageType.MAIN;
    }

     void updateOrder(Integer newOrder) {
        validateOrder(newOrder);
         if (newOrder.equals(this.order)) {
             throw new IllegalArgumentException("Updating the new order to be identical to the old order is not permitted.");
         }
        this.order = newOrder;
    }

    public boolean isMain() {
        return this.type == ImageType.MAIN;
    }

    private void validateUrl(String url) {
        if (url == null)
            throw new IllegalArgumentException("The URL cannot be a null value.");

        if (url.isBlank())
            throw new IllegalArgumentException("The URL cannot be an empty value or whitespace.");

        if (!IMAGE_URL_PATTERN.matcher(url).matches())
            throw new IllegalArgumentException("The URL provided is invalid.");
    }

    private void validateOrder(Integer order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must be a non-null.");
        }
        if (order < 0) {
            throw new IllegalArgumentException("Order must be a positive number.");
        }
        if (order > 3) {
            throw new ProductImageLimitExceededException("The image order cannot exceed the maximum limit of 4.");
        }
    }
}
