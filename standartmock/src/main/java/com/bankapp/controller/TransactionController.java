package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.model.Client;
//import com.bankapp.repository.ClientRepository;
//import com.bankapp.util.SessionManager;
import com.bankapp.repository.ClientRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
//    private final SessionManager sessionManager;
    private Client recipientClient;
    private Account recipientAccount;
    private final RestTemplate restTemplate;

    public TransactionController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    // 1️⃣ Получить список всех клиентов перед переводом
    @GetMapping("/clients")
    public List<Client> getAllClients() {
        return List.copyOf(ClientRepository.getAllClients());
//        return restTemplate.getForObject("http://localhost:8081/clients", List.class);
    }

    // 2️⃣ Выбрать получателя перевода по телефону и номеру счета
    @PostMapping("/select-recipient")
    public String selectRecipient(@RequestParam String username, @RequestParam String accountNumber) {
        String res = restTemplate.getForObject("http://localhost:8081/auth/isLogged", String.class);
        if (res.equals("не аутентифицирован")) {
            return "❌ Ошибка: Сначала войдите в систему!";
        }
        if (!res.equals("аутентифицирован")) {
            return "❌ Ошибка: попробуйте попозже";
        }
//        Optional<Client> recipientOpt = ClientRepository.findByUsername(username);
        Client clientFromAuthServer = restTemplate.getForObject("http://localhost:8081/client/" + username, Client.class);
        if (clientFromAuthServer == null) {
            return "❌ Ошибка: Получатель не найден! 47";
        }
        System.out.println(username);
        System.out.println(clientFromAuthServer);
        Optional<Client> recipientOpt = ClientRepository.findById(clientFromAuthServer.getId());
        System.out.println(recipientOpt.get());
        if (recipientOpt.isEmpty()) {
            return "❌ Ошибка: Получатель не найден! 52";
        }

        Client client = recipientOpt.get();

        Optional<Account> recipientAccountOpt = client.getAccounts()
                .stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst();

        if (recipientAccountOpt.isEmpty()) {
            return "❌ Ошибка: У получателя нет такого счета!";
        }

        this.recipientClient = client;
        this.recipientAccount = recipientAccountOpt.get();

        return "✅ Получатель выбран: " + recipientClient.getFullName() + " (Счет: " + recipientAccount.getAccountNumber() + ")";
    }

    // 3️⃣ Выполнить перевод (указать сумму и изменить баланс)
    @PostMapping("/transfer")
    public String transfer(@RequestParam double amount) {
//        if (!sessionManager.isLoggedIn()) {
//            return "❌ Ошибка: Сначала войдите в систему!";
//        }
        String authStatus = restTemplate.getForObject("http://localhost:8081/auth/isLogged", String.class);
        if (authStatus.equals("не аутентифицирован")) {
            return "❌ Ошибка: Сначала войдите в систему!";
        }

        if (recipientClient == null || recipientAccount == null) {
            return "❌ Ошибка: Сначала выберите получателя!";
        }

//        Client sender = sessionManager.getLoggedInClient();

        Client senderFromAuthServer = restTemplate.getForObject("http://localhost:8081/auth/currentClient", Client.class);
        if (senderFromAuthServer == null) {
            return "❌ Ошибка: Пользователь не авторизован!";
        }

        Optional<Client> senderOpt = ClientRepository.findById(senderFromAuthServer.getId());
        if (senderOpt.isEmpty()) {
            return "❌ Ошибка: Получатель не найден!";
        }

        Client sender = senderOpt.get();

        Optional<Account> senderAccountOpt = sender.getAccounts().stream().findFirst();
        if (senderAccountOpt.isEmpty()) {
            return "❌ Ошибка: У вас нет счета!";
        }

        Account senderAccount = senderAccountOpt.get();

        if (senderAccount.getBalance() < amount) {
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


        return "✅ Перевод завершен! " + amount + "₽ переведено на счет " + recipientAccount.getAccountNumber();
    }
}