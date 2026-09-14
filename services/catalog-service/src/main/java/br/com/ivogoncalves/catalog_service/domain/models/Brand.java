package br.com.ivogoncalves.catalog_service.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;

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
    private String description;
    @Getter
    private String logoUrl;
    private boolean active;
    @Getter
    private final Instant createdAt;
    @Getter
    private Instant updatedAt;

    private Brand(String name, String description, String logoUrl) {
        this.id = new BrandId();
        setName(name);
        this.description = description;
        this.logoUrl = logoUrl;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    public static Brand create(String name, String description, String logoUrl) {
        return new Brand(name, description, logoUrl);
    }

    public void updateName(String newName) {
        setName(newName);
        setUpdatedAt();
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
        setUpdatedAt();
    }

    public void updateLogo(String logoUrl) {
        this.logoUrl = logoUrl;
        setUpdatedAt();
    }

    public void activate() {
        if (this.active)
            throw new IllegalArgumentException("The brand is already active");
        this.active =  true;
        setUpdatedAt();
    }

     public void deactivate() {
        if (!this.active)
            throw new IllegalArgumentException("The brand is already inactive.");
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
