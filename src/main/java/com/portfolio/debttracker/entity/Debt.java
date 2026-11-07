package com.portfolio.debttracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "debts")
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DebtType type;

    @Column(name = "total_installments")
    private Integer totalInstallments;

    @Column(name = "current_installment")
    private Integer currentInstallment;

    public Debt() {
    }

    public Debt(Long id, String description, BigDecimal amount, DebtType type, Integer totalInstallments, Integer currentInstallment) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.totalInstallments = totalInstallments;
        this.currentInstallment = currentInstallment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public Integer getCurrentInstallment() {
        return currentInstallment;
    }

    public void setCurrentInstallment(Integer currentInstallment) {
        this.currentInstallment = currentInstallment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Debt debt = (Debt) o;
        return Objects.equals(id, debt.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Debt{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", totalInstallments=" + totalInstallments +
                ", currentInstallment=" + currentInstallment +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String description;
        private BigDecimal amount;
        private DebtType type;
        private Integer totalInstallments;
        private Integer currentInstallment;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder type(DebtType type) {
            this.type = type;
            return this;
        }

        public Builder totalInstallments(Integer totalInstallments) {
            this.totalInstallments = totalInstallments;
            return this;
        }

        public Builder currentInstallment(Integer currentInstallment) {
            this.currentInstallment = currentInstallment;
            return this;
        }

        public Debt build() {
            return new Debt(id, description, amount, type, totalInstallments, currentInstallment);
        }
    }
}
