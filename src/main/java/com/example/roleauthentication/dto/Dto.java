package com.example.roleauthentication.dto;

import com.example.roleauthentication.entity.Task;
import com.example.roleauthentication.entity.User;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class Dto {

    public record RegisterRequest(
            @NotBlank(message = "Name is required")
            @Size(min = 2, max = 60, message = "Name must be 2-60 characters")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record AuthResponse(
            String token,
            String type,
            String email,
            String name,
            String role
    ) {
        public static AuthResponse of(String token, User user) {
            return new AuthResponse(token, "Bearer", user.getEmail(), user.getName(), user.getRole().name());
        }
    }

    public record TaskRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 200, message = "Title can't exceed 200 characters")
            String title,

            @Size(max = 2000, message = "Description too long")
            String description,

            Task.Status status,
            Task.Priority priority
    ) {}

    public record TaskResponse(
            Long id,
            String title,
            String description,
            String status,
            String priority,
            Long userId,
            String userName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static TaskResponse from(Task task) {
            return new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus().name(),
                    task.getPriority().name(),
                    task.getUser().getId(),
                    task.getUser().getName(),
                    task.getCreatedAt(),
                    task.getUpdatedAt()
            );
        }
    }

    public record UserResponse(
            Long id,
            String name,
            String email,
            String role,
            LocalDateTime createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                    user.getRole().name(), user.getCreatedAt());
        }
    }

    public record ApiResponse<T>(
            boolean success,
            String message,
            T data
    ) {
        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, "Success", data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }


    public record PageResponse<T>(
            java.util.List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}
}
