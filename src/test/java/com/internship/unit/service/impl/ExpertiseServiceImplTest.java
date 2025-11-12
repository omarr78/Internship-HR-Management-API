package com.internship.unit.service.impl;

import com.internship.entity.Expertise;
import com.internship.repository.ExpertiseRepository;
import com.internship.service.impl.ExpertiseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertiseServiceImplTest {

    @Mock
    private ExpertiseRepository expertiseRepository;

    @InjectMocks
    private ExpertiseServiceImpl service;

    private static final String EMPTY_STRING = "";

    private Expertise buildExpertise(String expertiseName) {
        return Expertise.builder()
                .name(expertiseName)
                .build();
    }

    @Test
    public void testGetExpertiseWithEmptyExpertiseName_shouldIgnoreExpertiseNameAndReturnEmptyList() {
        // Given
        List<String> expertiseNames = List.of(EMPTY_STRING, EMPTY_STRING);
        // action
        List<Expertise> expertiseList = service.getExpertises(expertiseNames);
        // then
        assertNotNull(expertiseList);
        assertEquals(List.of(), expertiseList);
    }

    @Test
    public void testGetNotExistingExpertise_shouldAddExpertiseAndReturnExpertiseList() {
        // Given
        Expertise expertise1 = buildExpertise("Java");
        Expertise expertise2 = buildExpertise("Spring boot");

        List<String> expertiseNames = List.of(expertise1.getName(), expertise2.getName());

        when(expertiseRepository.findExpertiseByName(any(String.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        lenient().when(expertiseRepository.save(any(Expertise.class)))
                .thenReturn(expertise1)
                .thenReturn(expertise2);

        // action
        List<Expertise> expertiseList = service.getExpertises(expertiseNames);

        // then
        assertNotNull(expertiseList);
        assertEquals(List.of(expertise1, expertise2), expertiseList);
    }

    @Test
    public void testGetExistingExpertise_shouldReturnExpertiseList() {
        // Given
        Expertise expertise1 = buildExpertise("Java");
        Expertise expertise2 = buildExpertise("Spring boot");

        List<String> expertiseNames = List.of(expertise1.getName(), expertise2.getName());

        when(expertiseRepository.findExpertiseByName(any(String.class)))
                .thenReturn(Optional.of(expertise1))
                .thenReturn(Optional.of(expertise2));
        lenient().when(expertiseRepository.save(any(Expertise.class)))
                .thenReturn(expertise1)
                .thenReturn(expertise2);

        // action
        List<Expertise> expertiseList = service.getExpertises(expertiseNames);

        // then
        assertNotNull(expertiseList);
        assertEquals(List.of(expertise1, expertise2), expertiseList);
    }
}