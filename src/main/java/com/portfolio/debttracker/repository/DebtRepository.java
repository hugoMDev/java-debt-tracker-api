package com.portfolio.debttracker.repository;

import com.portfolio.debttracker.entity.Debt;
import com.portfolio.debttracker.entity.DebtType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    /**
     * Efficiently calculates the total sum of all debts directly in the database.
     * Returns 0 if there are no registered debts.
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Debt d")
    BigDecimal sumTotalAmount();

    /**
     * Counts debts by type (FIXA or PARCELADA).
     */
    long countByType(DebtType type);
}
