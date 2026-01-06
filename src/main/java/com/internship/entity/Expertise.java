package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.internship.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static com.internship.exception.ApiError.INVALID_DATA;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "EXPERTISES")
public class Expertise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToMany(mappedBy = "expertises")
    @JsonIgnore
    private List<Employee> employees;

    @PrePersist
    @PreUpdate
    private void validateEmptyExpertise() {
        if (name.isEmpty()) {
            throw new BusinessException(INVALID_DATA, "Invalid Expertises Name");
        }
    }
}