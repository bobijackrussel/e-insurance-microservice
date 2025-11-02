package com.einsurance.user.service;

import com.einsurance.common.dto.UserDto;
import com.einsurance.common.dto.UserRegistrationRequest;
import com.einsurance.common.dto.UserUpdateRequest;
import com.einsurance.common.exception.ResourceAlreadyExistsException;
import com.einsurance.common.exception.ResourceNotFoundException;
import com.einsurance.user.entity.User;
import com.einsurance.user.mapper.UserMapper;
import com.einsurance.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .keycloakId("keycloak-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+38761234567")
                .role("CUSTOMER")
                .isActive(true)
                .build();

        testUserDto = UserDto.builder()
                .id(testUser.getId())
                .keycloakId(testUser.getKeycloakId())
                .email(testUser.getEmail())
                .firstName(testUser.getFirstName())
                .lastName(testUser.getLastName())
                .phone(testUser.getPhone())
                .role(testUser.getRole())
                .isActive(testUser.getIsActive())
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .keycloakId("keycloak-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+38761234567")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByKeycloakId(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRegistrationRequest.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        // When
        UserDto result = userService.registerUser(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUserDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenKeycloakIdExists() {
        // Given
        when(userRepository.existsByKeycloakId(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, 
            () -> userService.registerUser(registrationRequest));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenEmailExists() {
        // Given
        when(userRepository.existsByKeycloakId(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, 
            () -> userService.registerUser(registrationRequest));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(testUserDto.getId(), result.getId());
        assertEquals(testUserDto.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_ThrowsException_WhenNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> userService.getUserById(userId));
    }

    @Test
    void getUserByKeycloakId_Success() {
        // Given
        when(userRepository.findByKeycloakId(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserByKeycloakId("keycloak-123");

        // Then
        assertNotNull(result);
        assertEquals(testUserDto.getKeycloakId(), result.getKeycloakId());
    }

    @Test
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testUserDto.getEmail(), result.getEmail());
    }

    @Test
    void deactivateUser_Success() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(userId);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        assertFalse(testUser.getIsActive());
    }

    @Test
    void activateUser_Success() {
        // Given
        UUID userId = testUser.getId();
        testUser.setIsActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.activateUser(userId);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        assertTrue(testUser.getIsActive());
    }

    @Test
    void getUserStatistics_Success() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByIsActiveTrue()).thenReturn(95L);
        when(userRepository.countByRole("ADMIN")).thenReturn(5L);
        when(userRepository.countByRole("CUSTOMER")).thenReturn(95L);

        // When
        UserStatistics stats = userService.getUserStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(95L, stats.getActiveUsers());
        assertEquals(5L, stats.getAdminUsers());
        assertEquals(95L, stats.getCustomerUsers());
    }
}