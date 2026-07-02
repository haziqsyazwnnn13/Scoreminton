package com.example.Scoreminton.repositories;

import com.example.Scoreminton.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // Finds all players created by a specific user account
    List<Player> findByUserId(Long userId);
}