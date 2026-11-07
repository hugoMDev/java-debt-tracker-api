package com.portfolio.debttracker.dto;

import com.portfolio.debttracker.entity.DebtType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response payload representing a registered debt")
public record DebtResponseDTO(

        @Schema(description = "Unique identifier of the debt", example = "1")
        Long id,

        @Schema(description = "Description or title of the debt", example = "Internet Bill")
        String description,

        @Schema(description = "Debt monetary amount", example = "120.00")
        BigDecimal amount,

        @Schema(description = "Debt classification type", example = "FIXA")
        DebtType type,

        @Schema(description = "Total number of installments (null for FIXA)", example = "null")
        Integer totalInstallments,

        @Schema(description = "Current installment (null for FIXA)", example = "null")
        Integer currentInstallment,

        @Schema(description = "Calculated remaining installments to finish (null for FIXA)", example = "null")
        Integer remainingInstallments
) {
}
