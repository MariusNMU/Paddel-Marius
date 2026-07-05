package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.parametre.ParametresMetierResponse;
import com.padelMarius.backend.service.ParametresMetierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parametres-metier")
@RequiredArgsConstructor
public class ParametresMetierController {

    private final ParametresMetierService parametresMetierService;

    @GetMapping
    public ParametresMetierResponse consulterParametresMetier() {
        return parametresMetierService.consulterParametresMetier();
    }
}