package br.com.ivogoncalves.catalog_service.domain.models;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

/**
 * @author Ivo Gonçalves
 */
public record ProductImageId(UUID id) {

    public ProductImageId() {
        this(UuidCreator.getTimeOrderedEpoch());
    }
}
