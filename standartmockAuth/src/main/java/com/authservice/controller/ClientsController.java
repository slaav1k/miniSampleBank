package com.authservice.controller;

import com.authservice.model.Client;
import com.authservice.repository.ClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ClientsController {
    @GetMapping("/clients")
    public List<Client> getAllClients() {
        return new ArrayList<>(ClientRepository.getAllClients());
    }

    @GetMapping("/client/{username}")
    public Client getClientByUsername(@PathVariable String username) {
        System.out.println(username);
        System.out.println(ClientRepository.getAllClients());
        System.out.println(ClientRepository.findByUsername(username));
        return ClientRepository.findByUsername(username).orElse(null);
    }


}
