package com.bankapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Operation(
            summary = "Приветственное сообщение",
            description = "Возвращает приветственное сообщение для указанного имени пользователя. Если имя не указано, используется значение по умолчанию 'Гость'."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешное приветственное сообщение",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Приветствие с указанным именем",
                                    value = """
                                    {
                                        "message": "Привет, Иван!"
                                    }
                                    """
                            ),
                            @ExampleObject(
                                    name = "Приветствие с именем по умолчанию",
                                    value = """
                                    {
                                        "message": "Привет, Гость!"
                                    }
                                    """
                            )
                    }
            )
    )
    @GetMapping("/hello")
    public String sayHello(@RequestParam(defaultValue = "Гость") String name) {
        return "Привет, " + name + "!";
    }
}