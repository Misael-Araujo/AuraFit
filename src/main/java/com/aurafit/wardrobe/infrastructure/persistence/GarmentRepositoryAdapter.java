package com.aurafit.wardrobe.infrastructure.persistence;

import com.aurafit.wardrobe.application.GarmentFilter;
import com.aurafit.wardrobe.application.GarmentRepositoryPort;
import com.aurafit.wardrobe.domain.Garment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class GarmentRepositoryAdapter implements GarmentRepositoryPort {
  private final GarmentJpaRepository repository;

  public GarmentRepositoryAdapter(GarmentJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Garment save(Garment garment) {
    return repository.save(GarmentEntity.fromAggregate(garment)).toAggregate();
  }

  @Override
  public Optional<Garment> findById(UUID id) {
    return repository.findById(id).map(GarmentEntity::toAggregate);
  }

  @Override
  public Page<Garment> findOwned(UUID ownerUserId, GarmentFilter filter, Pageable pageable) {
    return repository
        .findAll(GarmentSpecifications.forUser(ownerUserId, filter), pageable)
        .map(GarmentEntity::toAggregate);
  }

  @Override
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
