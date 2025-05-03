package com.authservice.controller;

import com.authservice.model.Client;
import com.authservice.repository.ClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ClientsController {
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех зарегистрированных клиентов"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список всех клиентов",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Список клиентов",
                            value = """
                            [
                                {
                                    "id": "123e4567-e89b-12d3-a456-426614174000",
                                    "fullName": "Иван Иванов",
                                    "phone": "+79001234567",
                                    "username": "ivan123",
                                    "password": "12345",
                                    "accounts": []
                                },
                                {
                                    "id": "223e4567-e89b-12d3-a456-426614174001",
                                    "fullName": "Мария Петрова",
                                    "phone": "+79009876543",
                                    "username": "maria456",
                                    "password": "67890",
                                    "accounts": []
                                }
                            ]
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "204",
            description = "Список клиентов пуст",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Пустой список",
                            value = "[]"
                    )
            )
    )
    @GetMapping("/clients")
    public List<Client> getAllClients() {
        return new ArrayList<>(ClientRepository.getAllClients());
    }

    @Operation(
            summary = "Получить пользователя по username",
            description = "Возвращает пользователя по имени пользователя, если он найден"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Клиент найден",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Клиент найден",
                            value = """
                            {
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "fullName": "Иван Иванов",
                                "phone": "+79001234567",
                                "username": "ivan123",
                                "password": "12345",
                                "accounts": []
                            }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Клиент не найден",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Клиент не найден",
                            value = """
                            {
                                "error": "Клиент с именем ivan123 не найден"
                            }
                            """
                    )
            )
    )
    @GetMapping("/client/{username}")
    public Client getClientByUsername(
            @Parameter(description = "Имя пользователя клиента", example = "ivan123")
            @PathVariable String username) {
        System.out.println(username);
        System.out.println(ClientRepository.getAllClients());
        System.out.println(ClientRepository.findByUsername(username));
        return ClientRepository.findByUsername(username).orElse(null);
    }


}
