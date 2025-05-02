package com.authservice.service;

import com.authservice.model.Client;
import com.authservice.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {

    public Client register(String fullName, String phone, String username, String password) {
        Client client = new Client(fullName, phone, username, password);
        return ClientRepository.save(client);
    }

    public Client register(Client client) {
        return ClientRepository.save(client);
    }


    public Optional<Client> login(String username, String password) {
        return ClientRepository.findByUsername(username)
                .filter(client -> client.getPassword().equals(password));
    }
}