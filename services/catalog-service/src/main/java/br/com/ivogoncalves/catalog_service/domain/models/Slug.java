package br.com.ivogoncalves.catalog_service.domain.models;

import java.text.Normalizer;

/**
 * @author Ivo Gonçalves
 */
public record Slug(String value) {

    public Slug {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Slug value cannot be empty.");
    }

    public static Slug create(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Text for slug generation is required.");
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return new Slug(normalized);
    }
}
