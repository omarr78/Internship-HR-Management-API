package com.internship.service;

import com.internship.dto.CreateBonusRequest;
import com.internship.dto.CreateBonusResponse;
import com.internship.entity.Bonus;
import com.internship.entity.Employee;
import com.internship.exception.BusinessException;
import com.internship.mapper.BonusMapper;
import com.internship.repository.BonusRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.validation.aspect.ValidateBonusDate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.internship.exception.ApiError.EMPLOYEE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BonusService {
    private final BonusRepository bonusRepository;
    private final BonusMapper bonusMapper;
    private final EmployeeRepository employeeRepository;

    @Transactional
    @ValidateBonusDate
    public CreateBonusResponse addBonus(final CreateBonusRequest request) {
        Long id = request.getEmployeeId();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        Bonus bonus = request.getBonusDate() == null ?
                bonusMapper.toEntity(LocalDate.now(), request.getAmount(), employee) :
                bonusMapper.toEntity(request.getBonusDate(), request.getAmount(), employee);

        bonusRepository.save(bonus);
        return bonusMapper.toResponse(bonus);
    }
}
