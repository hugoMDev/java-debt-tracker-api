package com.portfolio.debttracker.entity;

/**
 * Represents the category of the debt.
 * - FIXA: Recurring fixed expenses like rent, water, electricity, internet.
 * - PARCELADA: Split installment purchases via credit card or payment slips.
 */
public enum DebtType {
    FIXA,
    PARCELADA
}
