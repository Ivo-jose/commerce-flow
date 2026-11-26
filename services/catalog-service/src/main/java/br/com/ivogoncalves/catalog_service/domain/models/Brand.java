package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.exceptions.BrandAlreadyActiveException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.BrandAlreadyInactiveException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Ivo Gonçalves
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Brand {

    @Getter
    @EqualsAndHashCode.Include
    private final BrandId id;
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
        setDescription(description);
        setLogoUrl(logoUrl);
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    private Brand(BrandId id, String name, String description, String logoUrl, Boolean active, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Id cannot be null.");
        setName(name);
        setDescription(description);
        setLogoUrl(logoUrl);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "Create at cannot be null.");
        this.updatedAt = updatedAt;
    }

    public static Brand create(String name, String description, String logoUrl) {
        return new Brand(name, description, logoUrl);
    }

    public static Brand reconstruction(
            BrandId id,
            String name,
            String description,
            String logoUrl,
            Boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        Objects.requireNonNull(active, "Active cannot be null.");
        return new Brand(id, name, description, logoUrl, active, createdAt, updatedAt);
    }

    public void updateName(String newName) {
        setName(newName);
        setUpdatedAt();
    }

    public void updateDescription(String newDescription) {
        String normalized = newDescription == null || newDescription.isBlank() ? null : newDescription.trim();
        if (Objects.equals(this.description, normalized)) {
            throw new IllegalArgumentException("The Brand's new description can't be its current description.");
        }
        setDescription(normalized);
        setUpdatedAt();
    }

    public void updateLogo(String logoUrl) {
        String normalized = logoUrl == null || logoUrl.isBlank() ? null : logoUrl.trim();
        if (Objects.equals(this.logoUrl, normalized)) {
            throw new IllegalArgumentException("The Brand's new logo url can't be its current logo url.");
        }
        setLogoUrl(normalized);
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
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name required!");
        }

        String normalizedName = name.trim();

        if (Objects.equals(this.name, normalizedName)) {
            throw new IllegalArgumentException("The Brand's new name can't be its current name.");
        }

        this.name = normalizedName;
        this.slug = Slug.create(this.name);
    }

    private void setDescription(String description) {
        this.description =  description ==  null || description.isBlank() ? null : description.trim();
    }

    private void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl == null || logoUrl.isBlank() ? null : logoUrl.trim();
    }

    private void setUpdatedAt() {
        this.updatedAt =  Instant.now();
    }
}
