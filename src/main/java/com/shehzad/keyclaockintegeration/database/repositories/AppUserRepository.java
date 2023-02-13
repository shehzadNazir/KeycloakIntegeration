package com.shehzad.keyclaockintegeration.database.repositories;

import com.shehzad.keyclaockintegeration.database.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByKeycloakUserID(String keycloakUserID);
    public boolean existsByEmail(String email);
}
