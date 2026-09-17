package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.exceptions.BrandAlreadyActiveException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.BrandAlreadyInactiveException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Ivo Gonçalves
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Brand {

    @Getter
    @EqualsAndHashCode.Include
    private BrandId id;
    @Getter
    private String name;
    @Getter
    private Slug slug;
    @Getter
    private Optional<String> description;
    @Getter
    private Optional<String> logoUrl;
    private boolean active;
    @Getter
    private final Instant createdAt;
    @Getter
    private Instant updatedAt;

    private Brand(String name, Optional<String> description, Optional<String> logoUrl) {
        this.id = new BrandId();
        setName(name);
        this.description = description;
        this.logoUrl = logoUrl;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    public static Brand create(String name, Optional<String> description, Optional<String> logoUrl) {
        return new Brand(name, description, logoUrl);
    }

    public void updateName(String newName) {
        setName(newName);
        setUpdatedAt();
    }

    public void updateDescription(Optional<String> newDescription) {
        this.description = newDescription;
        setUpdatedAt();
    }

    public void updateLogo(Optional<String> logoUrl) {
        this.logoUrl = logoUrl;
        setUpdatedAt();
    }

    public void activate() {
        if (this.active)
            throw new BrandAlreadyActiveException("The brand is already active");
        this.active =  true;
        setUpdatedAt();
    }

     public void deactivate() {
        if (!this.active)
            throw new BrandAlreadyInactiveException("The brand is already inactive.");
        this.active = false;
        setUpdatedAt();
    }

    public boolean isActive() {
        return this.active;
    }

    private void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name required!");
        this.name = name.trim();
        this.slug = Slug.create(this.name);
    }

    private void setUpdatedAt() {
        this.updatedAt =  Instant.now();
    }
}
