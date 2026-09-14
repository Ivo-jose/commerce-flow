package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.enums.DimensionUnit;

import java.math.BigDecimal;

/**
 * @author Ivo Gonçalves
 */
public record Dimensions(BigDecimal length, BigDecimal width, BigDecimal height, DimensionUnit unit) {

    public Dimensions {
        if (length == null || length.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Length must be greater than zero.");
        }
        if (width == null || width.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Width must be greater than zero.");
        }
        if (height == null || height.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Height must be greater than zero.");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null or empty.");
        }
    }

    public BigDecimal calculateVolume() {
        return length().multiply(height()).multiply(width());
    }
}