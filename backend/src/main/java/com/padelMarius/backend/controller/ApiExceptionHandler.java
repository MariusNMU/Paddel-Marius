package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.ApiErrorResponse;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse gererRessourceIntrouvable(
            RessourceIntrouvableException exception
    ) {
        return new ApiErrorResponse(
                "RESSOURCE_INTROUVABLE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ConfigurationMetierException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse gererConfigurationMetier(
            ConfigurationMetierException exception
    ) {
        return new ApiErrorResponse(
                "CONFIGURATION_METIER_INVALIDE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(AuthentificationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse gererAuthentification(
            AuthentificationException exception
    ) {
        return new ApiErrorResponse(
                "AUTHENTIFICATION_INVALIDE",
                exception.getMessage()
        );
    }
}