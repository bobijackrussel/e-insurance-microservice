package com.einsurance.user.controller;

import com.einsurance.common.dto.UserRegistrationRequest;
import com.einsurance.user.entity.User;
import com.einsurance.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .keycloakId("test-keycloak-id")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("+38761234567")
                .role("CUSTOMER")
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_Success() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .keycloakId("new-keycloak-id")
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .phone("+38761234567")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("New"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void getCurrentUser_Success() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllUsers_AsAdmin_Success() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void getAllUsers_AsCustomer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getUserStatistics_AsAdmin_Success() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(get("/api/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").isNumber());
    }
}