package br.com.ivogoncalves.catalog_service.application.ports.output.product;

import java.util.List;

/**
 * @author Ivo Gonçalves
 */
public record ProductPageOut(
        List<ProductOut> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

}
