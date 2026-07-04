package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.ApiErrorResponse;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String MESSAGE_VALIDATION_INVALIDE =
            "La requête contient des données invalides.";

    private static final String MESSAGE_JSON_INVALIDE =
            "Le corps JSON de la requête est invalide ou illisible.";

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

    @ExceptionHandler(AutorisationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse gererAutorisation(
            AutorisationException exception
    ) {
        return new ApiErrorResponse(
                "ACCES_REFUSE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse gererValidationBody(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(this::formaterErreurChamp)
                .orElse(MESSAGE_VALIDATION_INVALIDE);

        return new ApiErrorResponse(
                "VALIDATION_INVALIDE",
                message
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse gererViolationContrainte(
            ConstraintViolationException exception
    ) {
        String message = exception.getConstraintViolations()
                .stream()
                .findFirst()
                .map(this::formaterViolationContrainte)
                .orElse(MESSAGE_VALIDATION_INVALIDE);

        return new ApiErrorResponse(
                "VALIDATION_INVALIDE",
                message
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse gererValidationMethode(
            HandlerMethodValidationException exception
    ) {
        return new ApiErrorResponse(
                "VALIDATION_INVALIDE",
                MESSAGE_VALIDATION_INVALIDE
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse gererParametreObligatoireAbsent(
            MissingServletRequestParameterException exception
    ) {
        return new ApiErrorResponse(
                "REQUETE_INVALIDE",
                "Le paramètre obligatoire '" + exception.getParameterName() + "' est absent."
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse gererParametreTypeInvalide(
            MethodArgumentTypeMismatchException exception
    ) {
        return new ApiErrorResponse(
                "REQUETE_INVALIDE",
                "Le paramètre '" + exception.getName() + "' a une valeur invalide."
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse gererJsonInvalide(
            HttpMessageNotReadableException exception
    ) {
        return new ApiErrorResponse(
                "JSON_INVALIDE",
                MESSAGE_JSON_INVALIDE
        );
    }

    private String formaterErreurChamp(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();

        if (message == null || message.isBlank()) {
            return "Le champ '" + fieldError.getField() + "' est invalide.";
        }

        return "Le champ '" + fieldError.getField() + "' est invalide : " + message;
    }

    private String formaterViolationContrainte(ConstraintViolation<?> violation) {
        String chemin = violation.getPropertyPath() == null
                ? "paramètre"
                : violation.getPropertyPath().toString();

        String message = violation.getMessage();

        if (message == null || message.isBlank()) {
            return "Le paramètre '" + chemin + "' est invalide.";
        }

        return "Le paramètre '" + chemin + "' est invalide : " + message;
    }
}