package com.habitbeat.backend.auth.repository;

import com.habitbeat.backend.auth.model.PasswordCredential;
import com.habitbeat.backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordCredentialRepository extends JpaRepository<PasswordCredential, UUID> {
    Optional<PasswordCredential> findByUser(User user);
}