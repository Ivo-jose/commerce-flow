package br.com.ivogoncalves.catalog_service.application.ports.output;

import br.com.ivogoncalves.catalog_service.domain.models.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Ivo Gonçalves
 */
public record ProductOut(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal basePrice,
        BigDecimal weight,
        BigDecimal volume,
        String dimensionUnit,
        String brand,
        String category,
        String status,
        Instant addedOn,
        List<ProductImageOut> images
) {

    public record ProductImageOut(
            UUID id,
            String url,
            Integer order,
            String type
    ) {
    }

    public static ProductOut from(Product product, String brand, String category) {
        return new ProductOut(
                product.getId().id(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                product.getWeight(),
                product.getDimensions().calculateVolume(),
                product.getDimensions().unit().name(),
                brand,
                category,
                product.getStatus().name(),
                product.getCreatedAt(),
                product.getImages().stream().map(img -> {
                    return new ProductImageOut(
                            img.getId().id(),
                            img.getUrl(),
                            img.getOrder(),
                            img.getType().name()
                    );
                }).toList()
        );
    }
}
