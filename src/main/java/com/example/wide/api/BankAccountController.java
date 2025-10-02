package com.example.wide.api;

import com.example.wide.dto.BankAccountDto;
import com.example.wide.entities.BankAccount;
import com.example.wide.enumeration.UserRole;
import com.example.wide.exception.BusinessException;
import com.example.wide.security.Principal;
import com.example.wide.service.BankAccountManagementService;
import com.example.wide.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final Logger logger = LogManager.getLogger(BankAccountController.class);

    private final BankAccountManagementService bankAccountManagementService;
    private final JwtUtil jwtUtil;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user bank accounts by userId",
            description = "Fetch bank accounts belongs to user by their unique ID. Admin allowed to access on behalf.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Bank accounts found.",
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
    public ResponseEntity<?> getBankAccounts(@PathVariable("userId") Long userId,
                                             Pageable pageable,
                                             HttpServletRequest request) {
        Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        String token = jwtUtil.extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }
        if(userId==null || userId.compareTo(0L) <= 0) {
            return ResponseEntity.badRequest().body("User ID is null");
        }

        GrantedAuthority adminRole = new SimpleGrantedAuthority(UserRole.ADMIN.getCode());
        if(!principal.getId().equals(userId) && !principal.getAuthorities().contains(adminRole)) {
            throw new BusinessException("Forbidden Access.");
        }

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(bankAccountManagementService.listUserBankAccounts(username, userId, pageable));
    }
}
