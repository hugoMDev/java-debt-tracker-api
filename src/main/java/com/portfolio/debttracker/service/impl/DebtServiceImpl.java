package com.portfolio.debttracker.service.impl;

import com.portfolio.debttracker.dto.DebtRequestDTO;
import com.portfolio.debttracker.dto.DebtResponseDTO;
import com.portfolio.debttracker.dto.DebtSummaryDTO;
import com.portfolio.debttracker.entity.Debt;
import com.portfolio.debttracker.entity.DebtType;
import com.portfolio.debttracker.exception.BusinessException;
import com.portfolio.debttracker.exception.ResourceNotFoundException;
import com.portfolio.debttracker.mapper.DebtMapper;
import com.portfolio.debttracker.repository.DebtRepository;
import com.portfolio.debttracker.service.DebtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DebtServiceImpl implements DebtService {

    private static final Logger log = LoggerFactory.getLogger(DebtServiceImpl.class);

    private final DebtRepository debtRepository;
    private final DebtMapper debtMapper;

    public DebtServiceImpl(DebtRepository debtRepository, DebtMapper debtMapper) {
        this.debtRepository = debtRepository;
        this.debtMapper = debtMapper;
    }

    @Override
    @Transactional
    public DebtResponseDTO createDebt(DebtRequestDTO request) {
        log.info("Creating new debt: '{}' of type '{}'", request.description(), request.type());

        validateDebtRules(request);

        Debt debt = debtMapper.toEntity(request);
        Debt saved = debtRepository.save(debt);

        log.info("Debt registered successfully with ID: {}", saved.getId());
        return debtMapper.toResponseDTO(saved);
    }

    @Override
    public List<DebtResponseDTO> getAllDebts() {
        log.debug("Fetching all registered debts");
        return debtRepository.findAll()
                .stream()
                .map(debtMapper::toResponseDTO)
                .toList();
    }

    @Override
    public DebtResponseDTO getDebtById(Long id) {
        log.debug("Fetching debt with ID: {}", id);
        return debtRepository.findById(id)
                .map(debtMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Debt", id));
    }

    @Override
    @Transactional
    public void deleteDebt(Long id) {
        log.info("Attempting to delete debt with ID: {}", id);
        if (!debtRepository.existsById(id)) {
            throw new ResourceNotFoundException("Debt", id);
        }
        debtRepository.deleteById(id);
        log.info("Debt with ID {} was deleted successfully", id);
    }

    @Override
    public DebtSummaryDTO getDebtsSummary() {
        log.debug("Calculating debts summary");

        BigDecimal totalAmount = debtRepository.sumTotalAmount();
        long totalCount = debtRepository.count();
        long fixedCount = debtRepository.countByType(DebtType.FIXA);
        long installmentCount = debtRepository.countByType(DebtType.PARCELADA);

        return new DebtSummaryDTO(totalAmount, totalCount, fixedCount, installmentCount);
    }

    private void validateDebtRules(DebtRequestDTO request) {
        if (request.type() == DebtType.PARCELADA) {
            if (request.totalInstallments() == null || request.totalInstallments() < 1) {
                throw new BusinessException("Installment debts (PARCELADA) must have totalInstallments of at least 1");
            }

            if (request.currentInstallment() == null || request.currentInstallment() < 1) {
                throw new BusinessException("Installment debts (PARCELADA) must have currentInstallment of at least 1");
            }

            if (request.currentInstallment() > request.totalInstallments()) {
                throw new BusinessException(String.format(
                        "Current installment (%d) cannot be greater than total installments (%d)",
                        request.currentInstallment(), request.totalInstallments()
                ));
            }
        }
    }
}
