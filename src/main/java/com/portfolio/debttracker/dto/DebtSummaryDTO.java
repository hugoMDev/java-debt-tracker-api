package com.portfolio.debttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Summary payload with the total sum and debt count statistics")
public record DebtSummaryDTO(

        @Schema(description = "Total accumulated monetary sum of all debts", example = "1065.50")
        BigDecimal totalAmount,

        @Schema(description = "Total number of debts registered", example = "4")
        long totalDebtsCount,

        @Schema(description = "Count of recurring fixed debts", example = "2")
        long fixedDebtsCount,

        @Schema(description = "Count of installment debts", example = "2")
        long installmentDebtsCount
) {
}
