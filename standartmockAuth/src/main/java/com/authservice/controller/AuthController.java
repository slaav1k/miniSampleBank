package com.authservice.controller;

import com.authservice.model.Client;
import com.authservice.service.ClientService;
import com.authservice.util.SessionManager;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final ClientService clientService;
    private final SessionManager sessionManager;
    private final MeterRegistry meterRegistry;
    private int responseTime = 1;

    public AuthController(ClientService clientService, SessionManager sessionManager, MeterRegistry meterRegistry) {
        this.clientService = clientService;
        this.sessionManager = sessionManager;
        this.meterRegistry = meterRegistry;
    }

//    @Operation(
//            summary = "Регистрация через форму",
//            description = "Регистрирует нового пользователя через параметры формы",
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Форма регистрации",
//                    content = @Content(
//                            mediaType = "application/x-www-form-urlencoded",
//                            examples = @ExampleObject(
//                                    name = "Форма регистрации",
//                                    value = "fullName=Иван Иванов&phone=+79001234567&username=ivan123&password=12345"
//                            )
//                    )
//            )
//    )
//    @PostMapping("/register/form")
//    public Client register(@RequestParam String fullName, @RequestParam String phone,
//                           @RequestParam String username, @RequestParam String password) {
//        return clientService.register(fullName, phone, username, password);
//    }



    @Operation(
            summary = "Регистрация через JSON",
            description = "Регистрирует нового пользователя через JSON-объект",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON с данными клиента",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Данные клиента",
                                    value = """
                                    {
                                        "fullName": "Иван Иванов",
                                        "phone": "+79001234567",
                                        "username": "ivan123",
                                        "password": "12345"
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешная регистрация",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Успешная регистрация",
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
            responseCode = "400",
            description = "Ошибка: Пользователь уже существует",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ошибка регистрации",
                            value = """
                            {
                                "error": "Пользователь с именем ivan123 уже существует"
                            }
                            """
                    )
            )
    )
    @PostMapping("/register")
    public Client register(@RequestBody Client client) {
//        return clientService.register(client);
        meterRegistry.counter("auth_register_json_requests_total", "application", "auth-service", "endpoint", "/register").increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        Client result = clientService.register(client);
        sample.stop(meterRegistry.timer("auth_register_json_duration", "application", "auth-service", "endpoint", "/register"));
        return result;
    }


    @Operation(
            summary = "Установить таймаут ответа",
            description = "Изменяет задержку в миллисекундах перед ответами сервера (для симуляции задержек)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Таймаут успешно установлен",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Успешный ответ",
                            value = """
                            {
                                "message": ""
                            }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка: Некорректное значение таймаута",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ошибка таймаута",
                            value = """
                            {
                                "error": "Таймаут должен быть положительным числом"
                            }
                            """
                    )
            )
    )
    @PostMapping("/setTimeout")
    public ResponseEntity<String> setTimeout(
            @Parameter(description = "Время задержки в миллисекундах", example = "1000")
            @RequestParam Integer timeout) {
        meterRegistry.counter("auth_set_timeout_requests_total", "application", "auth-service", "endpoint", "/setTimeout").increment();
        responseTime = timeout;
        return ResponseEntity.ok("");
    }

    @Operation(
            summary = "Вход в систему",
            description = "Авторизует пользователя по имени пользователя и паролю",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Параметры входа",
                    content = @Content(mediaType = "application/x-www-form-urlencoded")
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный вход в систему",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                            {
                                "message": "✅ Успешный вход: ivan123"
                            }
                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Ошибка: Неверный логин или пароль",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                            {
                                "message": "❌ Ошибка: Неверный логин или пароль"
                            }
                            """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/login")
    public String login(
            @Parameter(description = "Имя пользователя", example = "ivan123")
            @RequestParam String username,
            @Parameter(description = "Пароль", example = "12345")
            @RequestParam String password
    ) {
        meterRegistry.counter("auth_login_requests_total", "application", "auth-service", "endpoint", "/login").increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Thread.sleep(responseTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Optional<Client> clientOpt = clientService.login(username, password);
//        if (clientOpt.isPresent()) {
//            sessionManager.login(clientOpt.get());
//            return "✅ Успешный вход: " + username;
//        }
//        return "❌ Ошибка: Неверный логин или пароль";
        String result = clientOpt.map(client -> {
            sessionManager.login(client);
            return "✅ Успешный вход: " + username;
        }).orElse("❌ Ошибка: Неверный логин или пароль");

        sample.stop(meterRegistry.timer("auth_login_duration", "application", "auth-service", "endpoint", "/login"));
        return result;
    }


    @Operation(
            summary = "Проверка статуса авторизации",
            description = "Возвращает, аутентифицирован ли текущий пользователь"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Статус авторизации",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Аутентифицирован",
                                    value = """
                                    {
                                        "status": "аутентифицирован"
                                    }
                                    """
                            ),
                            @ExampleObject(
                                    name = "Не аутентифицирован",
                                    value = """
                                    {
                                        "status": "не аутентифицирован"
                                    }
                                    """
                            )
                    }
            )
    )
    @GetMapping("/isLogged")
    public String isLogged() {
        meterRegistry.counter("auth_is_logged_requests_total", "application", "auth-service", "endpoint", "/isLogged").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Thread.sleep(responseTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        return sessionManager.isLoggedIn() ? "аутентифицирован" : "не аутентифицирован";
        String status = sessionManager.isLoggedIn() ? "аутентифицирован" : "не аутентифицирован";
        sample.stop(meterRegistry.timer("auth_is_logged_duration", "application", "auth-service", "endpoint", "/isLogged"));
        return status;
    }

    @Operation(
            summary = "Возвращает текущего пользователя",
            description = "Возвращает информацию о текущем аутентифицированном пользователе"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Информация о текущем пользователе",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Аутентифицированный пользователь",
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
                            ),
                            @ExampleObject(
                                    name = "Не аутентифицирован",
                                    value = """
                                    null
                                    """
                            )
                    }
            )
    )
    @GetMapping("/currentClient")
    public Client getCurrentClient() {
        meterRegistry.counter("auth_current_client_requests_total", "application", "auth-service", "endpoint", "/currentClient").increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Thread.sleep(responseTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        return sessionManager.isLoggedIn() ? sessionManager.getLoggedInClient() : null;
        Client client = sessionManager.isLoggedIn() ? sessionManager.getLoggedInClient() : null;
        sample.stop(meterRegistry.timer("auth_current_client_duration", "application", "auth-service", "endpoint", "/currentClient"));
        return client;
    }

    @Operation(
            summary = "Выход из системы",
            description = "Разлогинивает текущего пользователя"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный выход",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Успешный выход",
                            value = """
                            {
                                "message": "✅ Успешный выход"
                            }
                            """
                    )
            )
    )
    @PostMapping("/logout")
    public String logout() {
        meterRegistry.counter("auth_logout_requests_total", "application", "auth-service", "endpoint", "/logout").increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Thread.sleep(responseTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sessionManager.logout();
        sample.stop(meterRegistry.timer("auth_logout_duration", "application", "auth-service", "endpoint", "/logout"));
        return "✅ Успешный выход";
    }
}