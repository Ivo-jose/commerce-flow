package br.com.ivogoncalves.catalog_service.domain.models;

import br.com.ivogoncalves.catalog_service.domain.exceptions.CategoryAlreadyActiveException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.CategoryAlreadyInactiveException;
import br.com.ivogoncalves.catalog_service.domain.exceptions.InvalidCategoryHierarchyException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Ivo Gonçalves
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {

    @Getter
    @EqualsAndHashCode.Include
    private final CategoryId id;
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
        this.slug = Slug.create(this.name);
        this.parentCategoryId = parentCategoryId;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    private Category(CategoryId id , String name, Slug slug, CategoryId parentCategoryId, Boolean active, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Id cannot be null.");
        setName(name);
        this.slug = Objects.requireNonNull(slug, "It is not permitted to rebuild the category with a null slug.");
        this.parentCategoryId = parentCategoryId;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt =  updatedAt;
    }

    public static Category create(String name, CategoryId parentCategoryId) {
        return new Category(name, parentCategoryId);
    }

    public static Category reconstruction(
            CategoryId id,
            String name,
            Slug slug,
            CategoryId parentCategoryId,
            Boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        Objects.requireNonNull(active, "Active cannot be null.");
        return  new Category(id, name, slug, parentCategoryId, active, createdAt, updatedAt);
    }

    public void updateName(String newName) {
        String normalized =  newName == null || newName.isBlank() ? null : newName.trim();
        if (Objects.equals(this.name, normalized)) {
            throw new IllegalArgumentException("The new category name cannot be the same as the current category name.");
        }
        setName(normalized);
        this.slug = Slug.create(normalized);
        setUpdatedAt();
    }

    public void activate() {
        if (this.active)
            throw new CategoryAlreadyActiveException("The category is already active.");
        this.active = true;
        setUpdatedAt();
    }

    public void deactivate() {
        if (!this.active)
            throw new CategoryAlreadyInactiveException("The category is already inactive.");
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
        validateParent(newParentCategoryId);
        this.parentCategoryId = newParentCategoryId;
        setUpdatedAt();
    }

    private void validateParent(CategoryId parentId) {
        if (Objects.equals(this.id, parentId)) {
            throw new InvalidCategoryHierarchyException("A category cannot be a parent of itself.");
        }

        if (Objects.equals(this.parentCategoryId, parentId)) {
            throw new IllegalArgumentException("The new parent is already the current parent.");
        }
    }

    private void setName(String name) {
        String normalized = name == null || name.isBlank() ? null : name.trim();
        if (normalized == null) {
            throw new IllegalArgumentException("The name is required.");
        }
        this.name = normalized;
    }
}