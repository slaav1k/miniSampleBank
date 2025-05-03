package com.authservice.controller;

import com.authservice.model.Client;
import com.authservice.repository.ClientRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class ClientsController {
    private static final Logger logger = LoggerFactory.getLogger(ClientsController.class);

    private final ClientRepository clientRepository;
    private final MeterRegistry meterRegistry;

    @Autowired
    public ClientsController(ClientRepository clientRepository, MeterRegistry meterRegistry) {
        this.clientRepository = clientRepository;
        this.meterRegistry = meterRegistry;
    }

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
    @Observed(name = "clients.list", contextualName = "clients#list", lowCardinalityKeyValues = {"operation", "list"})
    public List<Client> getAllClients() {
        logger.info("Запрос на получение списка всех клиентов");
        meterRegistry.counter("clients_list_requests_total", "endpoint", "/clients").increment();

        // Добавление Timer для измерения времени выполнения
        Timer timer = meterRegistry.timer("clients_list_request_duration", "endpoint", "/clients");
        long startTime = System.nanoTime();
        try {
            List<Client> clients = new ArrayList<>(clientRepository.getAllClients());
            logger.debug("Возвращено {} клиентов", clients.size());
            return clients;
        } finally {
            long duration = System.nanoTime() - startTime;
            timer.record(duration, TimeUnit.NANOSECONDS);
        }
    }

    @Operation(
            summary = "Получить пользователя по имени пользователя",
            description = "Возвращает информацию о клиенте по его имени пользователя"
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
    @Observed(name = "clients.get", contextualName = "clients#get", lowCardinalityKeyValues = {"operation", "get"})
    public Client getClientByUsername(
            @Parameter(description = "Имя пользователя клиента", example = "ivan123")
            @PathVariable String username) {
        logger.info("Запрос клиента по username: {}", username);
        meterRegistry.counter("client_get_requests_total", "endpoint", "/client/" + username).increment();

        // Добавление Gauge для отслеживания количества клиентов в памяти
        io.micrometer.core.instrument.Gauge.builder(
                        "clients_in_memory_count",
                        clientRepository,
                        repo -> repo.getAllClients().size()
                )
                .tags(List.of(
                        Tag.of("endpoint", "/client/" + username),
                        Tag.of("application", "auth-service")
                ))
                .description("Number of clients in memory")
                .register(meterRegistry);

        // Добавление DistributionSummary для распределения размера списка клиентов
        meterRegistry.summary("clients_list_size_distribution", "endpoint", "/client/" + username).record(clientRepository.getAllClients().size());

        Client client = clientRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Клиент с username {} не найден", username);
                    return new RuntimeException("Клиент с именем " + username + " не найден");
                });
        logger.debug("Найден клиент: {}", client);
        return client;
    }
}