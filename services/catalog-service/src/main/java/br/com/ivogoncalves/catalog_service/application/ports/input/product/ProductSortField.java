package br.com.ivogoncalves.catalog_service.application.ports.input.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Ivo Gonçalves
 */
@Getter
@AllArgsConstructor
public enum ProductSortField {

    NAME,
    PRICE,
    CREATED_AT;
}