package com.portfolio.debttracker.mapper;

import com.portfolio.debttracker.dto.DebtRequestDTO;
import com.portfolio.debttracker.dto.DebtResponseDTO;
import com.portfolio.debttracker.entity.Debt;
import com.portfolio.debttracker.entity.DebtType;
import org.springframework.stereotype.Component;

@Component
public class DebtMapper {

    public Debt toEntity(DebtRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        boolean isParcelada = dto.type() == DebtType.PARCELADA;

        return Debt.builder()
                .description(dto.description().trim())
                .amount(dto.amount())
                .type(dto.type())
                .totalInstallments(isParcelada ? dto.totalInstallments() : null)
                .currentInstallment(isParcelada ? dto.currentInstallment() : null)
                .build();
    }

    public DebtResponseDTO toResponseDTO(Debt debt) {
        if (debt == null) {
            return null;
        }

        Integer remainingInstallments = null;
        if (debt.getType() == DebtType.PARCELADA
                && debt.getTotalInstallments() != null
                && debt.getCurrentInstallment() != null) {
            remainingInstallments = Math.max(0, debt.getTotalInstallments() - debt.getCurrentInstallment());
        }

        return new DebtResponseDTO(
                debt.getId(),
                debt.getDescription(),
                debt.getAmount(),
                debt.getType(),
                debt.getTotalInstallments(),
                debt.getCurrentInstallment(),
                remainingInstallments
        );
    }
}
