package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.enums.ImageType;
import br.com.ivogoncalves.catalog_service.domain.exceptions.ProductImageLimitExceededException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

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
        this.url = url;
        this.type = ImageType.GALLERY;
        this.order = order;
    }

    public static ProductImage create(String url, Integer order) {
        return new ProductImage(url, order);
    }

     void switchToGallery() {
        this.type = ImageType.GALLERY;
    }

     void switchToMain() {
        this.type = ImageType.MAIN;
    }

     void updateOrder(Integer newOrder) {
        validateOrder(newOrder);
        this.order = newOrder;
    }

    public boolean isMain() {
        return this.type == ImageType.MAIN;
    }

    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty() || ! IMAGE_URL_PATTERN.matcher(url).matches())
            throw new IllegalArgumentException("The URL provided is invalid.");
    }

    private void validateOrder(Integer order) {
        if (order == null || order < 0) {
            throw new IllegalArgumentException("Order must be a non-null positive integer.");
        }
        if (order > 3) {
            throw new ProductImageLimitExceededException("The image order cannot exceed the maximum limit of 4.");
        }
    }
}
