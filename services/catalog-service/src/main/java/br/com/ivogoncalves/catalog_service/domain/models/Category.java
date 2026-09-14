package br.com.ivogoncalves.catalog_service.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ivo Gonçalves
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {

    @Getter
    @EqualsAndHashCode.Include
    private CategoryId id;
    @Getter
    private String name;
    @Getter
    private Slug slug;
    @Getter
    private CategoryId parentCategoryId;
    private boolean active;
    @Getter
    private final Instant createdAt;
    @Getter
    private Instant updatedAt;

    private Category(String name, CategoryId parentCategoryId) {
        this.id = new CategoryId();
        setName(name);
        this.parentCategoryId = parentCategoryId;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    public static Category create(String name, CategoryId parentCategoryId) {
        return new Category(name, parentCategoryId);
    }

    public void updateName(String newName) {
        setName(newName);
        setUpdatedAt();
    }

    public void activate() {
        if (this.active)
            throw new IllegalArgumentException("The category is already active.");
        this.active = true;
        setUpdatedAt();
    }

    public void deactivate() {
        if (!this.active)
            throw new IllegalArgumentException("The category is already inactive.");
        this.active = false;
        setUpdatedAt();
    }

    public boolean isActive() {
        return this.active;
    }

    private void setUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public void changeParent(CategoryId newParentCategoryId) {
        if (this.id.equals(newParentCategoryId))
            throw new IllegalArgumentException("A category cannot be a parent of itself.");
        this.parentCategoryId = newParentCategoryId;
        setUpdatedAt();
    }

    private void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required!");
        this.name = name.trim();
        this.slug = Slug.create(this.name);
    }
}