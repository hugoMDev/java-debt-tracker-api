package com.portfolio.debttracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.debttracker.dto.DebtRequestDTO;
import com.portfolio.debttracker.dto.DebtResponseDTO;
import com.portfolio.debttracker.dto.DebtSummaryDTO;
import com.portfolio.debttracker.entity.DebtType;
import com.portfolio.debttracker.exception.GlobalExceptionHandler;
import com.portfolio.debttracker.exception.ResourceNotFoundException;
import com.portfolio.debttracker.service.DebtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DebtController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("DebtController Unit & Web MVC Tests")
class DebtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DebtService debtService;

    @Test
    @DisplayName("POST /api/debts - Should return 201 Created when payload is valid")
    void shouldCreateDebtWhenPayloadIsValid() throws Exception {
        DebtRequestDTO request = new DebtRequestDTO(
                "Gym Membership",
                new BigDecimal("99.90"),
                DebtType.FIXA,
                null,
                null
        );

        DebtResponseDTO response = new DebtResponseDTO(
                1L,
                "Gym Membership",
                new BigDecimal("99.90"),
                DebtType.FIXA,
                null,
                null,
                null
        );

        when(debtService.createDebt(any(DebtRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/debts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Gym Membership")))
                .andExpect(jsonPath("$.amount", is(99.90)))
                .andExpect(jsonPath("$.type", is("FIXA")));
    }

    @Test
    @DisplayName("POST /api/debts - Should return 400 Bad Request when description is empty")
    void shouldReturn400WhenDescriptionIsBlank() throws Exception {
        DebtRequestDTO request = new DebtRequestDTO(
                "",
                new BigDecimal("50.00"),
                DebtType.FIXA,
                null,
                null
        );

        mockMvc.perform(post("/api/debts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.description").exists());
    }

    @Test
    @DisplayName("GET /api/debts - Should return 200 OK and list of debts")
    void shouldReturnAllDebts() throws Exception {
        List<DebtResponseDTO> debts = List.of(
                new DebtResponseDTO(1L, "Rent", new BigDecimal("1200.00"), DebtType.FIXA, null, null, null),
                new DebtResponseDTO(2L, "Sofa", new BigDecimal("200.00"), DebtType.PARCELADA, 10, 2, 8)
        );

        when(debtService.getAllDebts()).thenReturn(debts);

        mockMvc.perform(get("/api/debts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Rent")))
                .andExpect(jsonPath("$[1].description", is("Sofa")));
    }

    @Test
    @DisplayName("GET /api/debts/{id} - Should return 200 OK when debt exists")
    void shouldReturnDebtById() throws Exception {
        DebtResponseDTO response = new DebtResponseDTO(
                1L,
                "Rent",
                new BigDecimal("1200.00"),
                DebtType.FIXA,
                null,
                null,
                null
        );

        when(debtService.getDebtById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/debts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Rent")));
    }

    @Test
    @DisplayName("GET /api/debts/{id} - Should return 404 Not Found when debt does not exist")
    void shouldReturn404WhenDebtNotFound() throws Exception {
        when(debtService.getDebtById(999L)).thenThrow(new ResourceNotFoundException("Debt", 999L));

        mockMvc.perform(get("/api/debts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Debt with ID 999 not found")));
    }

    @Test
    @DisplayName("DELETE /api/debts/{id} - Should return 204 No Content on successful deletion")
    void shouldReturn204WhenDeletedSuccessfully() throws Exception {
        doNothing().when(debtService).deleteDebt(1L);

        mockMvc.perform(delete("/api/debts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/debts/total - Should return 200 OK with total sum and breakdown")
    void shouldReturnTotalSummary() throws Exception {
        DebtSummaryDTO summary = new DebtSummaryDTO(
                new BigDecimal("1400.00"),
                2L,
                1L,
                1L
        );

        when(debtService.getDebtsSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/debts/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount", is(1400.00)))
                .andExpect(jsonPath("$.totalDebtsCount", is(2)))
                .andExpect(jsonPath("$.fixedDebtsCount", is(1)))
                .andExpect(jsonPath("$.installmentDebtsCount", is(1)));
    }
}
