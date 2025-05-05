package com.authservice.service;

import com.authservice.model.Client;
import com.authservice.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public ClientService(ClientRepository clientRepository, BCryptPasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Client register(String fullName, String phone, String username, String password) {
        // Client client = new Client(fullName, phone, username, passwordEncoder.encode(password));
        Client client = new Client(fullName, phone, username, password);
        return clientRepository.save(client);
    }

    public Client register(Client client) {
        // client.setPassword(passwordEncoder.encode(client.getPassword()));
        client.setPassword(client.getPassword());
        return clientRepository.save(client);
    }

    public Optional<Client> login(String username, String password) {
        // return clientRepository.findByUsername(username)
        //         .filter(client -> passwordEncoder.matches(password, client.getPassword()));
        return clientRepository.findByUsername(username)
                .filter(client -> password.equals(client.getPassword()));
    }
}