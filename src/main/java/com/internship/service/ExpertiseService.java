package com.internship.service;

import com.internship.entity.Expertise;
import com.internship.repository.ExpertiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpertiseService {
    private final ExpertiseRepository expertiseRepository;

    @Transactional
    public List<Expertise> getExpertises(List<String> expertiseNames) {
        List<Expertise> expertises = new ArrayList<>();
        for (String expertiseName : expertiseNames) {
            expertiseRepository.findExpertiseByName(expertiseName).ifPresent(expertises::add);
        }
        return expertises;
    }

    public List<Expertise> createNotFoundExpertise(List<String> expertiseNames) {
        List<Expertise> expertises = new ArrayList<>();
        for (String expertiseName : expertiseNames) {
            Optional<Expertise> optional = expertiseRepository.findExpertiseByName(expertiseName);
            if (optional.isEmpty()) {
                Expertise exp = Expertise.builder().name(expertiseName).build();
                expertiseRepository.save(exp);
                expertises.add(exp);
            }
        }
        return expertises;
    }
}
