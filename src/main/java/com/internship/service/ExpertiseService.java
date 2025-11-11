package com.internship.service;

import com.internship.entity.Expertise;

import java.util.List;

public interface ExpertiseService {
    List<Expertise> getExpertises(List<String> expertiseNames);
}
