package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.match.CreerMatchRequest;
import com.padelMarius.backend.dto.match.MatchResponse;
import com.padelMarius.backend.service.MatchCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchCreationService matchCreationService;

    @PostMapping
    public ResponseEntity<MatchResponse> creerMatch(@Valid @RequestBody CreerMatchRequest request) {
        MatchResponse response = matchCreationService.creerMatch(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}