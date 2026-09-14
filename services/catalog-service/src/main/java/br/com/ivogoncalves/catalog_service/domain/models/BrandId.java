package br.com.ivogoncalves.catalog_service.domain.models;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * @author Ivo Gonçalves
 */
public record BrandId(UUID id) {

    public BrandId() {
        this(UuidCreator.getTimeOrderedEpoch());
    }
}
