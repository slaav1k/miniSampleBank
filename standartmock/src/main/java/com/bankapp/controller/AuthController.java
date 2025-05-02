//package com.bankapp.controller;
//
//import com.bankapp.model.Client;
//import com.bankapp.service.ClientService;
//import com.bankapp.util.SessionManager;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//    private final ClientService clientService;
//    private final SessionManager sessionManager;
//
//    public AuthController(ClientService clientService, SessionManager sessionManager) {
//        this.clientService = clientService;
//        this.sessionManager = sessionManager;
//    }
//
//    @Operation(summary = "Регистрация в системе",
//            description = "Регистрирует указанного пользователя в системе",
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Данные для регистрации",
//                    content = @Content(
//                            mediaType = "application/json")
//            ))
//    @PostMapping("/register")
//    public Client register(@RequestParam String fullName, @RequestParam String phone,
//                           @RequestParam String username, @RequestParam String password) {
//        return clientService.register(fullName, phone, username, password);
//    }
//    @PostMapping("/setTimeout")
//    public ResponseEntity<String> setTimeout(@RequestParam Integer timeout) {
//    //    tmp=timeout;
//        return ResponseEntity.ok("");
//    }
//    @PostMapping("/login")
//    public String login(@RequestParam String username, @RequestParam String password) {
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        Optional<Client> clientOpt = clientService.login(username, password);
//        if (clientOpt.isPresent()) {
//            sessionManager.login(clientOpt.get());
//            return "✅ Успешный вход: " + username;
//        }
//        return "❌ Ошибка: Неверный логин или пароль";
//    }
//
//    @PostMapping("/logout")
//    public String logout() {
//        sessionManager.logout();
//        return "✅ Успешный выход";
//    }
//}