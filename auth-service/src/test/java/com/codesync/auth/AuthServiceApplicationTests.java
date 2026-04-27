package com.codesync.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void registerEndpoint_PasswordTooShort_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content("""
                    {
                        "username": "testuser",
                        "email": "test@example.com",
                        "password": "123"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerEndpoint_InvalidEmail_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content("""
                    {
                        "username": "testuser",
                        "email": "invalid-email",
                        "password": "password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginEndpoint_MissingBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}