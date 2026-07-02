package com.example.Scoreminton.repositories;

import com.example.Scoreminton.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    // Finds all teams created by a specific user account
    List<Team> findByUserId(Long userId);
}