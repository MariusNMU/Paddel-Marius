package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.demo.PresentationDemoResponse;
import com.padelMarius.backend.service.PresentationDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/presentation")
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "padel.demo.enabled",
        havingValue = "true"
)
public class PresentationDemoController {

    private final PresentationDemoService presentationDemoService;

    @GetMapping
    public PresentationDemoResponse consulterPresentation() {
        return presentationDemoService.consulterPresentation();
    }
}
