package com.aurafit.outfit.infrastructure.persistence;

import com.aurafit.outfit.application.OutfitFilter;
import com.aurafit.outfit.application.OutfitRepositoryPort;
import com.aurafit.outfit.domain.Outfit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class OutfitRepositoryAdapter implements OutfitRepositoryPort {
  private final OutfitJpaRepository repository;

  public OutfitRepositoryAdapter(OutfitJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Outfit save(Outfit outfit) {
    return repository.save(OutfitEntity.fromAggregate(outfit)).toAggregate();
  }

  @Override
  public Optional<Outfit> findById(UUID id) {
    return repository.findById(id).map(OutfitEntity::toAggregate);
  }

  @Override
  public Page<Outfit> findOwned(UUID ownerUserId, OutfitFilter filter, Pageable pageable) {
    return repository
        .findAll(OutfitSpecifications.forUser(ownerUserId, filter), pageable)
        .map(OutfitEntity::toAggregate);
  }

  @Override
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
