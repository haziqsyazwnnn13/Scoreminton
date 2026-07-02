package com.example.Scoreminton.repositories;

import com.example.Scoreminton.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByUserId(Long userId);
}