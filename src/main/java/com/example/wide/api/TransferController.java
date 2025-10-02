package com.example.wide.api;


import com.example.wide.dto.TransferResponseDto;
import com.example.wide.enumeration.Currency;
import com.example.wide.enumeration.UserRole;
import com.example.wide.security.Principal;
import com.example.wide.service.TransferService;
import com.example.wide.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transfer")
public class TransferController {
    private final JwtUtil jwtUtil;
    private final TransferService transferService;

    public TransferController(JwtUtil jwtUtil, TransferService transferService) {
        this.jwtUtil = jwtUtil;
        this.transferService = transferService;
    }

    @PostMapping("/")
    @Operation(
            summary = "Transfer within same currency bank account",
            description = "Fund Transfer of particular currency within two different account regardless of account type",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TransferResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthenticated user"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Missing parameters"
                    )
            }
    )
    public ResponseEntity<?> transfer(@RequestParam("fromAccount") String sourceAccountNumber,
                                      @RequestParam("toAccount") String targetAccountNumber,
                                      @RequestParam("currency") Currency currency,
                                      @RequestParam("amount") BigDecimal amount,
                                      HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        if (sourceAccountNumber == null || sourceAccountNumber.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("From account number is mising.");
        }
        if (targetAccountNumber == null || targetAccountNumber.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Target account number is mising.");
        }
        if (currency == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing currency.");
        }
        if (amount == null || amount.compareTo(BigDecimal.valueOf(0)) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid transfer amount.");
        }

        String username = jwtUtil.extractUsername(token);
        TransferResponseDto response = transferService.transfer(username, sourceAccountNumber, targetAccountNumber, currency, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get transfer history by account number",
            description = "Fetch user transfer history by their bank account number. Admin allowed to access on behalf.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer history found.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = List.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthenticated user"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Missing parameters"
                    )
            }
    )
    public ResponseEntity<?> transferHistory(@RequestParam("account") String accountNumber, Pageable pageable, HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        if (accountNumber == null || accountNumber.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account number is mising.");
        }

        Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(transferService.listTransferHistory(username, principal.isAdmin(), accountNumber, pageable));
    }

}
