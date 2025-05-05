//package com.authservice.util;
//
//import com.authservice.model.Client;
//import com.authservice.model.Account;
//import com.authservice.repository.ClientRepository;
//import com.authservice.repository.AccountRepository;
//import com.github.javafaker.Faker;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Random;
//import java.util.UUID;
//
//@Component
//public class TestDataInitializer implements CommandLineRunner {
//    private final ClientRepository clientRepository;
//    private final AccountRepository accountRepository;
//    private final Faker faker = new Faker();
//    private final Random random = new Random();
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public TestDataInitializer(ClientRepository clientRepository, AccountRepository accountRepository) {
//        this.clientRepository = clientRepository;
//        this.accountRepository = accountRepository;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("📌 Генерация тестовых данных...");
//
//        for (int i = 0; i < 10; i++) {
//            // Генерируем данные клиента
//            String fullName = faker.name().fullName();
//            String phone = "+79" + (random.nextInt(900000000) + 100000000);
//            String username = "user" + (i + 1);
//            String password = "pass" + (i + 1);
//
//            // Создаем клиента
//            Client client = new Client(fullName, phone, username, password);
//            clientRepository.save(client);
//            System.out.println("✅ Создан клиент: " + fullName + " (" + phone + ") | Логин: " + username + ", Пароль: " + password);
//
//            // Создаем случайное количество счетов (от 1 до 3)
//            int accountCount = random.nextInt(3) + 1;
//            for (int j = 0; j < accountCount; j++) {
//                Account account = new Account();
//                account.setClient(client);
//                account.setBalance(random.nextInt(9000) + 1000.0); // Баланс от 1000 до 10000
//                account.setAccountNumber(UUID.randomUUID().toString());
//                account.setCardNumber("4000" + (random.nextInt(9000) + 1000)); // Пример номера карты
//                accountRepository.save(account);
//                client.getAccounts().add(account);
//                System.out.println("  ➕ Счет: " + account.getAccountNumber() + " | Карта: " + account.getCardNumber() + " | Баланс: " + account.getBalance() + "₽");
//            }
//
//            // Сохраняем обновленного клиента с привязанными счетами
//            clientRepository.save(client);
//
//            // Отправляем клиента в эндпоинт /auth/register
//            String url = "http://localhost:8081/auth/register";
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Client> request = new HttpEntity<>(client, headers);
//
//            try {
//                restTemplate.postForObject(url, request, Client.class);
//                System.out.println("📤 Клиент зарегистрирован в auth-service через /auth/register");
//            } catch (Exception e) {
//                System.out.println("❌ Ошибка регистрации в auth-service: " + e.getMessage());
//            }
//        }
//
//        System.out.println("🎉 Генерация тестовых данных завершена!");
//    }
//}
