package com.aurafit.identity.infrastructure.persistence;

import com.aurafit.identity.application.UserRepositoryPort;
import com.aurafit.identity.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class UserRepositoryAdapter implements UserRepositoryPort {

  private final UserJpaRepository repository;

  UserRepositoryAdapter(UserJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return repository.findByEmailIgnoreCase(email).map(UserEntity::toDomain);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return repository.findById(id).map(UserEntity::toDomain);
  }

  @Override
  public User save(User user) {
    return repository.save(UserEntity.from(user)).toDomain();
  }

  @Override
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
