package com.portfolio.debttracker.service;

import com.portfolio.debttracker.dto.DebtRequestDTO;
import com.portfolio.debttracker.dto.DebtResponseDTO;
import com.portfolio.debttracker.dto.DebtSummaryDTO;
import com.portfolio.debttracker.entity.Debt;
import com.portfolio.debttracker.entity.DebtType;
import com.portfolio.debttracker.exception.BusinessException;
import com.portfolio.debttracker.exception.ResourceNotFoundException;
import com.portfolio.debttracker.mapper.DebtMapper;
import com.portfolio.debttracker.repository.DebtRepository;
import com.portfolio.debttracker.service.impl.DebtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DebtService Unit Tests")
class DebtServiceTest {

    @Mock
    private DebtRepository debtRepository;

    @Spy
    private DebtMapper debtMapper;

    @InjectMocks
    private DebtServiceImpl debtService;

    private Debt fixedDebt;
    private Debt installmentDebt;

    @BeforeEach
    void setUp() {
        fixedDebt = Debt.builder()
                .id(1L)
                .description("Internet Bill")
                .amount(new BigDecimal("120.00"))
                .type(DebtType.FIXA)
                .totalInstallments(null)
                .currentInstallment(null)
                .build();

        installmentDebt = Debt.builder()
                .id(2L)
                .description("Laptop Purchase")
                .amount(new BigDecimal("350.00"))
                .type(DebtType.PARCELADA)
                .totalInstallments(10)
                .currentInstallment(3)
                .build();
    }

    @Nested
    @DisplayName("Create Debt Tests")
    class CreateDebtTests {

        @Test
        @DisplayName("Should successfully create a FIXA (fixed) debt")
        void shouldCreateFixedDebtSuccessfully() {
            DebtRequestDTO request = new DebtRequestDTO(
                    "Internet Bill",
                    new BigDecimal("120.00"),
                    DebtType.FIXA,
                    null,
                    null
            );

            when(debtRepository.save(any(Debt.class))).thenReturn(fixedDebt);

            DebtResponseDTO response = debtService.createDebt(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.description()).isEqualTo("Internet Bill");
            assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("120.00"));
            assertThat(response.type()).isEqualTo(DebtType.FIXA);
            assertThat(response.totalInstallments()).isNull();
            assertThat(response.remainingInstallments()).isNull();

            verify(debtRepository, times(1)).save(any(Debt.class));
        }

        @Test
        @DisplayName("Should successfully create a PARCELADA (installment) debt")
        void shouldCreateInstallmentDebtSuccessfully() {
            DebtRequestDTO request = new DebtRequestDTO(
                    "Laptop Purchase",
                    new BigDecimal("350.00"),
                    DebtType.PARCELADA,
                    10,
                    3
            );

            when(debtRepository.save(any(Debt.class))).thenReturn(installmentDebt);

            DebtResponseDTO response = debtService.createDebt(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(2L);
            assertThat(response.type()).isEqualTo(DebtType.PARCELADA);
            assertThat(response.totalInstallments()).isEqualTo(10);
            assertThat(response.currentInstallment()).isEqualTo(3);
            assertThat(response.remainingInstallments()).isEqualTo(7);

            verify(debtRepository, times(1)).save(any(Debt.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when totalInstallments is null for PARCELADA debt")
        void shouldThrowWhenTotalInstallmentsIsNullForParcelada() {
            DebtRequestDTO request = new DebtRequestDTO(
                    "Laptop",
                    new BigDecimal("350.00"),
                    DebtType.PARCELADA,
                    null,
                    1
            );

            assertThatThrownBy(() -> debtService.createDebt(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("totalInstallments of at least 1");

            verify(debtRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when currentInstallment is greater than totalInstallments")
        void shouldThrowWhenCurrentInstallmentGreaterThanTotal() {
            DebtRequestDTO request = new DebtRequestDTO(
                    "Laptop",
                    new BigDecimal("350.00"),
                    DebtType.PARCELADA,
                    5,
                    6
            );

            assertThatThrownBy(() -> debtService.createDebt(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be greater than total installments");

            verify(debtRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find Debts Tests")
    class FindDebtsTests {

        @Test
        @DisplayName("Should return all debts")
        void shouldReturnAllDebts() {
            when(debtRepository.findAll()).thenReturn(List.of(fixedDebt, installmentDebt));

            List<DebtResponseDTO> debts = debtService.getAllDebts();

            assertThat(debts).hasSize(2);
            assertThat(debts.get(0).description()).isEqualTo("Internet Bill");
            assertThat(debts.get(1).description()).isEqualTo("Laptop Purchase");

            verify(debtRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should find debt by ID when exists")
        void shouldFindDebtByIdWhenExists() {
            when(debtRepository.findById(1L)).thenReturn(Optional.of(fixedDebt));

            DebtResponseDTO response = debtService.getDebtById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.description()).isEqualTo("Internet Bill");

            verify(debtRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when debt ID does not exist")
        void shouldThrowWhenDebtNotFound() {
            when(debtRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> debtService.getDebtById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Debt with ID 999 not found");

            verify(debtRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Delete Debt Tests")
    class DeleteDebtTests {

        @Test
        @DisplayName("Should delete debt when ID exists")
        void shouldDeleteDebtWhenExists() {
            when(debtRepository.existsById(1L)).thenReturn(true);
            doNothing().when(debtRepository).deleteById(1L);

            debtService.deleteDebt(1L);

            verify(debtRepository, times(1)).existsById(1L);
            verify(debtRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent debt")
        void shouldThrowWhenDeletingNonExistentDebt() {
            when(debtRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> debtService.deleteDebt(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Debt with ID 999 not found");

            verify(debtRepository, times(1)).existsById(999L);
            verify(debtRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Debts Summary & Total Calculation Tests")
    class DebtsSummaryTests {

        @Test
        @DisplayName("Should correctly calculate total amount and count statistics")
        void shouldReturnDebtsSummaryCorrectly() {
            BigDecimal expectedTotal = new BigDecimal("470.00");

            when(debtRepository.sumTotalAmount()).thenReturn(expectedTotal);
            when(debtRepository.count()).thenReturn(2L);
            when(debtRepository.countByType(DebtType.FIXA)).thenReturn(1L);
            when(debtRepository.countByType(DebtType.PARCELADA)).thenReturn(1L);

            DebtSummaryDTO summary = debtService.getDebtsSummary();

            assertThat(summary).isNotNull();
            assertThat(summary.totalAmount()).isEqualByComparingTo(expectedTotal);
            assertThat(summary.totalDebtsCount()).isEqualTo(2L);
            assertThat(summary.fixedDebtsCount()).isEqualTo(1L);
            assertThat(summary.installmentDebtsCount()).isEqualTo(1L);

            verify(debtRepository, times(1)).sumTotalAmount();
            verify(debtRepository, times(1)).count();
            verify(debtRepository, times(1)).countByType(DebtType.FIXA);
            verify(debtRepository, times(1)).countByType(DebtType.PARCELADA);
        }
    }
}
