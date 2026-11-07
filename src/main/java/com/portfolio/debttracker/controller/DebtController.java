package com.portfolio.debttracker.controller;

import com.portfolio.debttracker.dto.DebtRequestDTO;
import com.portfolio.debttracker.dto.DebtResponseDTO;
import com.portfolio.debttracker.dto.DebtSummaryDTO;
import com.portfolio.debttracker.service.DebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debts")
@CrossOrigin(origins = "*")
@Tag(name = "Debts", description = "Endpoints for managing fixed and installment debts")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @PostMapping
    @Operation(summary = "Create a new debt", description = "Registers a new fixed (recurring) or installment debt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Debt created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation errors"),
            @ApiResponse(responseCode = "422", description = "Business rule validation failure")
    })
    public ResponseEntity<DebtResponseDTO> createDebt(@Valid @RequestBody DebtRequestDTO request) {
        DebtResponseDTO created = debtService.createDebt(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "List all debts", description = "Retrieves all registered debts.")
    @ApiResponse(responseCode = "200", description = "List of all debts retrieved")
    public ResponseEntity<List<DebtResponseDTO>> getAllDebts() {
        List<DebtResponseDTO> debts = debtService.getAllDebts();
        return ResponseEntity.ok(debts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get debt by ID", description = "Retrieves details of a specific debt by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Debt retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Debt not found")
    })
    public ResponseEntity<DebtResponseDTO> getDebtById(
            @Parameter(description = "ID of the debt to retrieve", example = "1")
            @PathVariable Long id) {
        DebtResponseDTO debt = debtService.getDebtById(id);
        return ResponseEntity.ok(debt);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete debt", description = "Removes a debt by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Debt removed successfully"),
            @ApiResponse(responseCode = "404", description = "Debt not found")
    })
    public ResponseEntity<Void> deleteDebt(
            @Parameter(description = "ID of the debt to delete", example = "1")
            @PathVariable Long id) {
        debtService.deleteDebt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    @Operation(summary = "Get total summary", description = "Calculates and returns the total accumulated sum of all debts and count breakdown.")
    @ApiResponse(responseCode = "200", description = "Debts summary returned successfully")
    public ResponseEntity<DebtSummaryDTO> getDebtsSummary() {
        DebtSummaryDTO summary = debtService.getDebtsSummary();
        return ResponseEntity.ok(summary);
    }
}
