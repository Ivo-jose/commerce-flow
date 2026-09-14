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
public enum ImageType {

    MAIN(0, "main"),
    GALLERY(1, "gallery");

    private final int code;
    private final String description;

    private static final Map<Integer, ImageType> LOOKUP_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(ImageType::getCode, Function.identity()));

    public static ImageType toEnum(Integer code) {
        if (code == null)
            return null;
        ImageType type = LOOKUP_MAP.get(code);
        if (type == null)
            throw new IllegalArgumentException("The provided code is invalid: " + code);
        return type;
    }
}
