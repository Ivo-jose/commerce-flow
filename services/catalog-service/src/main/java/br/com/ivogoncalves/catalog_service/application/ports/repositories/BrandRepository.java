package br.com.ivogoncalves.catalog_service.application.ports.repositories;

import br.com.ivogoncalves.catalog_service.domain.models.Brand;
import br.com.ivogoncalves.catalog_service.domain.models.BrandId;

import java.util.List;
import java.util.Optional;

/**
 * @author Ivo Gonçalves
 */
public interface BrandRepository {

    List<Brand> findAll();

    Optional<Brand> findById(BrandId brandId);

    Brand save(Brand brand);
}