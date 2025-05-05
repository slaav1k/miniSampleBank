package com.bankapp.service;

import com.bankapp.model.Account;
import com.bankapp.model.Client;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    public Account createAccount(String clientId) {
        UUID clientUUID;
        try {
            clientUUID = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid client ID format");
        }

        Optional<Client> clientOptional = clientRepository.findById(clientUUID);
        Client client = clientOptional.orElseThrow(() -> new RuntimeException("Client not found"));

        Account account = new Account();
        account.setClient(client);
        return accountRepository.save(account);
    }

    public Optional<Account> findByNumber(String number) {
        return accountRepository.findByAccountNumber(number);
    }
}
