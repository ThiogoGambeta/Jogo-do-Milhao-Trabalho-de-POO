package com.seuprojeto.millionaire.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HomeControllerTest {
    
    private final HomeController homeController = new HomeController();
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    
    @Test
    void shouldReturnHomePage() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
    }
}

