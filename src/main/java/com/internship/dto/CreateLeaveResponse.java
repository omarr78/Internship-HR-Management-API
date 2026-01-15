package com.internship.dto;

import com.internship.entity.Leave;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLeaveResponse {
    List<Leave> leaves;
}