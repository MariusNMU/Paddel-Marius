package com.padelMarius.backend.integration;

import com.padelMarius.backend.controller.PresentationDemoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "padel.demo.enabled=false")
@AutoConfigureMockMvc
class PresentationDemoDisabledIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotExposePresentationEndpointWhenDemoIsDisabled()
            throws Exception {
        assertThat(applicationContext.getBeansOfType(
                PresentationDemoController.class
        )).isEmpty();

        mockMvc.perform(get("/api/demo/presentation"))
                .andExpect(status().isNotFound());
    }
}
