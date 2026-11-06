package com.aurafit.identity.application;

import com.aurafit.identity.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID id);

  User save(User user);

  void deleteById(UUID id);
}
