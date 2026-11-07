package com.portfolio.debttracker.service;

import com.portfolio.debttracker.dto.DebtRequestDTO;
import com.portfolio.debttracker.dto.DebtResponseDTO;
import com.portfolio.debttracker.dto.DebtSummaryDTO;

import java.util.List;

public interface DebtService {

    /**
     * Creates and registers a new debt with appropriate business validations.
     *
     * @param request the debt details
     * @return the saved debt response
     */
    DebtResponseDTO createDebt(DebtRequestDTO request);

    /**
     * Retrieves all registered debts.
     *
     * @return list of debt responses
     */
    List<DebtResponseDTO> getAllDebts();

    /**
     * Retrieves a debt by its unique identifier.
     *
     * @param id the debt id
     * @return the found debt response
     */
    DebtResponseDTO getDebtById(Long id);

    /**
     * Deletes a debt by its unique identifier.
     *
     * @param id the debt id
     */
    void deleteDebt(Long id);

    /**
     * Calculates and returns the total accumulated amount and statistics of all debts.
     *
     * @return summary with total sum and debt counters
     */
    DebtSummaryDTO getDebtsSummary();
}
