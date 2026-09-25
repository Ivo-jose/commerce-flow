package br.com.ivogoncalves.catalog_service.application.ports.input.product;

/**
 * @author Ivo Gonçalves
 */
public record ProductPageRequest(int page, int size, ProductSortField sortField, ProductSortDirection sortDirection) {

    public ProductPageRequest {
        if (sortField == null) {
            sortField = ProductSortField.NAME;
        }

        if (sortDirection == null) {
            sortDirection = ProductSortDirection.ASC;
        }

        if (page < 0) {
            throw new IllegalArgumentException("The page value cannot be less than zero.");
        }

        if (size > 100) {
            throw new IllegalArgumentException("The size should be in the range of 1 to 100.");
        }
    }
}