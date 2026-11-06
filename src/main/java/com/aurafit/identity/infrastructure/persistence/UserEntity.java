package com.aurafit.identity.infrastructure.persistence;

import com.aurafit.identity.domain.User;
import com.aurafit.identity.domain.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class UserEntity {

  @Id private UUID id;

  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(name = "display_name", nullable = false, length = 80)
  private String displayName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private UserRole role;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Version private long version;

  protected UserEntity() {}

  private UserEntity(User user) {
    id = user.id();
    email = user.email();
    passwordHash = user.passwordHash();
    displayName = user.displayName();
    birthDate = user.birthDate();
    emailVerified = user.emailVerified();
    role = user.role();
    createdAt = user.createdAt();
  }

  static UserEntity from(User user) {
    return new UserEntity(user);
  }

  User toDomain() {
    return new User(
        id, email, passwordHash, displayName, birthDate, emailVerified, role, createdAt);
  }
}
