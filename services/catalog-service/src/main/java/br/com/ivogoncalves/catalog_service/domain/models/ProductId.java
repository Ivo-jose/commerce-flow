package br.com.ivogoncalves.catalog_service.domain.models;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Ivo Gonçalves
 */
public record ProductId(UUID id) {

    public ProductId {
        Objects.requireNonNull(id, "Product ID cannot be null.");
    }

    public ProductId() {
        this(UuidCreator.getTimeOrderedEpoch());
    }
}
