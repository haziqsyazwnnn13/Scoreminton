package com.example.Scoreminton.repositories;

import com.example.Scoreminton.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom method to find a user by their username for logging in
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}