package com.internship.service;

import com.internship.entity.Expertise;
import com.internship.repository.ExpertiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpertiseService {
    private final ExpertiseRepository expertiseRepository;

    @Transactional
    public List<Expertise> getExpertises(List<String> expertiseNames) {
        return expertiseRepository.findAllExpertiseByNameIn(expertiseNames);
    }

    public void createNotFoundExpertise(List<String> expertiseNames) {
        for (String expertiseName : expertiseNames) {
            Optional<Expertise> optional = expertiseRepository.findExpertiseByName(expertiseName);
            if (optional.isPresent()) continue;
            Expertise exp = Expertise.builder().name(expertiseName).build();
            expertiseRepository.save(exp);
        }
    }
}