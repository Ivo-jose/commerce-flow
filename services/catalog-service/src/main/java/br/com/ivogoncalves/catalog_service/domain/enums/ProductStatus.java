package br.com.ivogoncalves.catalog_service.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ivo Gonçalves
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {

    ACTIVE(0, "active"),
    INACTIVE(1, "inactive"),
    DISCONTINUED(2, "discontinued");

    private final int code;
    private final String description;

    private final static Map<Integer, ProductStatus> LOOKUP_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(ProductStatus::getCode, Function.identity()));

    public static ProductStatus toEnum(Integer code) {
        if (code == null)
            return null;
        ProductStatus status =  LOOKUP_MAP.get(code);
        if (status == null)
            throw new IllegalArgumentException("The code provide is invalid: " + code);
        return status;
    }
}
