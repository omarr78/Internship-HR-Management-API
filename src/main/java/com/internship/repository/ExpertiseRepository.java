package com.internship.repository;

import com.internship.entity.Expertise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertiseRepository extends JpaRepository<Expertise, Long> {
    Optional<Expertise> findExpertiseByName(String expertiseName);
}
