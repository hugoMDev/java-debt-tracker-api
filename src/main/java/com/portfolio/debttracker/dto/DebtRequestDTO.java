package com.portfolio.debttracker.dto;

import com.portfolio.debttracker.entity.DebtType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Payload to create or register a new debt")
public record DebtRequestDTO(

        @Schema(description = "Description or title of the debt", example = "Credit Card - Electronics")
        @NotBlank(message = "Description cannot be empty")
        @Size(max = 150, message = "Description must have at most 150 characters")
        String description,

        @Schema(description = "Amount of the debt or installment value", example = "350.00")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be strictly greater than zero")
        BigDecimal amount,

        @Schema(description = "Debt classification type: FIXA (Fixed recurring) or PARCELADA (Installment)", example = "PARCELADA")
        @NotNull(message = "Debt type is required (FIXA or PARCELADA)")
        DebtType type,

        @Schema(description = "Total number of installments (Mandatory if type is PARCELADA)", example = "10")
        Integer totalInstallments,

        @Schema(description = "Current installment being paid (Mandatory if type is PARCELADA)", example = "3")
        Integer currentInstallment
) {
}
