package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.model.Client;
import com.bankapp.repository.ClientRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private Client recipientClient;
    private Account recipientAccount;
    private final RestTemplate restTemplate;
    private final MeterRegistry meterRegistry; // Добавляем MeterRegistry для метрик
    private final ClientRepository clientRepository;

    public TransactionController(RestTemplate restTemplate, MeterRegistry meterRegistry, ClientRepository clientRepository) {
        this.restTemplate = restTemplate;
        this.meterRegistry = meterRegistry;
        this.clientRepository = clientRepository;
    }

    // Эндпоинт: GET /transactions/clients
    // Метрики: transactions_get_clients_requests_total (counter), transactions_get_clients_duration (timer)
    @Operation(
            summary = "Получить список всех клиентов",
            description = "Возвращает список всех зарегистрированных клиентов для выбора получателя перевода"
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
                                            "accounts": [
                                                {
                                                    "id": "acc1",
                                                    "accountNumber": "123456789012",
                                                    "cardNumber": "1234567890123456",
                                                    "balance": 5000.0
                                                }
                                            ]
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
        meterRegistry.counter("transactions_get_clients_requests_total", "application", "bank-app", "endpoint", "/clients").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        List<Client> clients = List.copyOf(clientRepository.findAll());

        sample.stop(meterRegistry.timer("transactions_get_clients_duration", "application", "bank-app", "endpoint", "/clients"));
        return clients;
    }

    // Эндпоинт: POST /transactions/select-recipient
    // Метрики: transactions_select_recipient_requests_total (counter), transactions_select_recipient_duration (timer)
    @Operation(
            summary = "Выбрать получателя перевода",
            description = "Выбирает получателя перевода по имени пользователя и номеру счета. Требуется авторизация."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Получатель успешно выбран",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Успешный выбор получателя",
                            value = """
                                    {
                                        "message": "✅ Получатель выбран: Иван Иванов (Счет: 123456789012)"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Ошибка: Пользователь не авторизован",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ошибка авторизации",
                            value = """
                                    {
                                        "error": "❌ Ошибка: Сначала войдите в систему!"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Ошибка: Получатель или счет не найдены",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Получатель не найден",
                                    value = """
                                            {
                                                "error": "❌ Ошибка: Получатель не найден!"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Счет не найден",
                                    value = """
                                            {
                                                "error": "❌ Ошибка: У получателя нет такого счета!"
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "503",
            description = "Ошибка: Сервис авторизации недоступен",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ошибка сервиса",
                            value = """
                                    {
                                        "error": "❌ Ошибка: попробуйте попозже"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/select-recipient")
    public String selectRecipient(
            @Parameter(description = "Имя пользователя получателя", example = "ivan123")
            @RequestParam String username,
            @Parameter(description = "Номер счета получателя", example = "123456789012")
            @RequestParam String accountNumber
    ) {
        meterRegistry.counter("transactions_select_recipient_requests_total", "application", "bank-app", "endpoint", "/select-recipient").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        String res = restTemplate.getForObject("http://localhost:8081/auth/isLogged", String.class);
        if (res.equals("не аутентифицирован")) {
            sample.stop(meterRegistry.timer("transactions_select_recipient_duration", "application", "bank-app", "endpoint", "/select-recipient"));
            return "❌ Ошибка: Сначала войдите в систему!";
        }
        if (!res.equals("аутентифицирован")) {
            sample.stop(meterRegistry.timer("transactions_select_recipient_duration", "application", "bank-app", "endpoint", "/select-recipient"));
            return "❌ Ошибка: попробуйте попозже";
        }

        Client clientFromAuthServer = restTemplate.getForObject("http://localhost:8081/client/" + username, Client.class);
        if (clientFromAuthServer == null) {
            sample.stop(meterRegistry.timer("transactions_select_recipient_duration", "application", "bank-app", "endpoint", "/select-recipient"));
            return "❌ Ошибка: Получатель не найден! 47";
        }
        System.out.println(username);
        System.out.println(clientFromAuthServer);
        Optional<Client> recipientOpt = clientRepository.findById(clientFromAuthServer.getId());
        System.out.println(recipientOpt.get());
        if (recipientOpt.isEmpty()) {
            sample.stop(meterRegistry.timer("transactions_select_recipient_duration", "application", "bank-app", "endpoint", "/select-recipient"));
            return "❌ Ошибка: Получатель не найден! 52";
        }

        Client client = recipientOpt.get();

        Optional<Account> recipientAccountOpt = client.getAccounts()
                .stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst();

        if (recipientAccountOpt.isEmpty()) {
            sample.stop(meterRegistry.timer("transactions_select_recipient_duration", "application", "bank-app", "endpoint", "/select-recipient"));
            return "❌ Ошибка: У получателя нет такого счета!";
        }

        this.recipientClient = client;
        this.recipientAccount = recipientAccountOpt.get();

        String result = "✅ Получатель выбран: " + recipientClient.getFullName() + " (Счет: " + recipientAccount.getAccountNumber() + ")";
        sample.stop(meterRegistry.timer("transactions_select_recipient_duration", "application", "bank-app", "endpoint", "/select-recipient"));
        return result;
    }

    // Эндпоинт: POST /transactions/transfer
    // Метрики: transactions_transfer_requests_total (counter), transactions_transfer_duration (timer)
    @Operation(
            summary = "Выполнить перевод",
            description = "Выполняет перевод указанной суммы от текущего пользователя к выбранному получателю. Требуется авторизация и предварительный выбор получателя."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Перевод успешно выполнен",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Успешный перевод",
                            value = """
                                    {
                                        "message": "✅ Перевод завершен! 1000.0₽ переведено на счет 123456789012"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Ошибка: Пользователь не авторизован",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ошибка авторизации",
                            value = """
                                    {
                                        "error": "❌ Ошибка: Сначала войдите в систему!"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка: Некорректные данные для перевода",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Получатель не выбран",
                                    value = """
                                            {
                                                "error": "❌ Ошибка: Сначала выберите получателя!"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Счет отправителя не найден",
                                    value = """
                                            {
                                                "error": "❌ Ошибка: У вас нет счета!"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Недостаточно средств",
                                    value = """
                                            {
                                                "error": "❌ Ошибка: Недостаточно средств на счете!"
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Ошибка: Пользователь не найден",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Отправитель не найден",
                            value = """
                                    {
                                        "error": "❌ Ошибка: Пользователь не авторизован!"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/transfer")
    public String transfer(
            @Parameter(description = "Сумма перевода в рублях", example = "1000.0")
            @RequestParam double amount) {
        meterRegistry.counter("transactions_transfer_requests_total", "application", "bank-app", "endpoint", "/transfer").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        String authStatus = restTemplate.getForObject("http://localhost:8081/auth/isLogged", String.class);
        if (authStatus.equals("не аутентифицирован")) {
            sample.stop(meterRegistry.timer("transactions_transfer_duration", "application", "bank-app", "endpoint", "/transfer"));
            return "❌ Ошибка: Сначала войдите в систему!";
        }

        if (recipientClient == null || recipientAccount == null) {
            sample.stop(meterRegistry.timer("transactions_transfer_duration", "application", "bank-app", "endpoint", "/transfer"));
            return "❌ Ошибка: Сначала выберите получателя!";
        }

        Client senderFromAuthServer = restTemplate.getForObject("http://localhost:8081/auth/currentClient", Client.class);
        if (senderFromAuthServer == null) {
            sample.stop(meterRegistry.timer("transactions_transfer_duration", "application", "bank-app", "endpoint", "/transfer"));
            return "❌ Ошибка: Пользователь не авторизован!";
        }

        Optional<Client> senderOpt = clientRepository.findById(senderFromAuthServer.getId());
        if (senderOpt.isEmpty()) {
            sample.stop(meterRegistry.timer("transactions_transfer_duration", "application", "bank-app", "endpoint", "/transfer"));
            return "❌ Ошибка: Получатель не найден!";
        }

        Client sender = senderOpt.get();

        Optional<Account> senderAccountOpt = sender.getAccounts().stream().findFirst();
        if (senderAccountOpt.isEmpty()) {
            sample.stop(meterRegistry.timer("transactions_transfer_duration", "application", "bank-app", "endpoint", "/transfer"));
            return "❌ Ошибка: У вас нет счета!";
        }

        Account senderAccount = senderAccountOpt.get();

        if (senderAccount.getBalance() < amount) {
            sample.stop(meterRegistry.timer("transactions_transfer_duration", "application", "bank-app", "endpoint", "/transfer"));
            return "❌ Ошибка: Недостаточно средств на счете!";
        }

        // Обновляем балансы
        System.out.println("💸 Перевод " + amount + "₽ от аккаунта " + senderAccount.getAccountNumber()
                + " к аккаунту " + recipientAccount.getAccountNumber());

        System.out.println("📊 Баланс отправителя ДО: " + senderAccount.getBalance() + "₽");
        System.out.println("📊 Баланс получателя ДО: " + recipientAccount.getBalance() + "₽");

        senderAccount.setBalance(senderAccount.getBalance() - amount);
        recipientAccount.setBalance(recipientAccount.getBalance() + amount);

        System.out.println("✅ Баланс отправителя ПОСЛЕ: " + senderAccount.getBalance() + "₽");
        System.out.println("✅ Баланс получателя ПОСЛЕ: " + recipientAccount.getBalance() + "₽");

        String result = "✅ Перевод завершен! " + amount + "₽ переведено на счет " + recipientAccount.getAccountNumber();
        sample.stop(meterRegistry.timer("transactions_transfer_duration", "application", "bank-app", "endpoint", "/transfer"));
        return result;
    }
}