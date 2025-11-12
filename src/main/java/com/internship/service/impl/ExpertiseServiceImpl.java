package com.internship.service.impl;

import com.internship.entity.Expertise;
import com.internship.repository.ExpertiseRepository;
import com.internship.service.ExpertiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpertiseServiceImpl implements ExpertiseService {
    private final ExpertiseRepository expertiseRepository;

    @Transactional
    @Override
    public List<Expertise> getExpertises(List<String> expertiseNames) {
        List<Expertise> expertises = new ArrayList<>();

        for (String expertiseName : expertiseNames) {
            // make sure that expertise name is not empty
            if (!expertiseName.isEmpty()) {
                Optional<Expertise> optional = expertiseRepository.findExpertiseByName(expertiseName);
                if (optional.isPresent()) {
                    expertises.add(optional.get());
                } else {
                    Expertise exp = Expertise.builder().name(expertiseName).build();
                    Expertise savedExpertise = expertiseRepository.save(exp);
                    expertises.add(savedExpertise);
                }
            }
        }
        return expertises;
    }
}
