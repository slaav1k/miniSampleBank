package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    public AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @Operation(
            summary = "Создание банковского счёта",
            description = "Создаёт банковский счёт для указанного клиента по clientId",
            parameters = {
                    @Parameter(
                            name = "clientId",
                            description = "Идентификатор клиента",
                            example = "client123"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное создание счёта",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "accountId": "acc456",
                                                        "clientId": "client123",
                                                        "balance": 0.0
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/create")
    public Account create(@RequestParam String clientId) {
        return accountService.createAccount(clientId);
    }
}
