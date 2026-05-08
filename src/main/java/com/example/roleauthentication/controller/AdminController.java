package com.example.roleauthentication.controller;
import com.example.roleauthentication.dto.Dto;
import com.example.roleauthentication.repository.UserRepository;
import com.example.roleauthentication.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin-only endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final TaskService taskService;
    private final UserRepository userRepo;

    public AdminController(TaskService taskService, UserRepository userRepo) {
        this.taskService = taskService;
        this.userRepo = userRepo;
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get all tasks from all users")
    public ResponseEntity<Dto.ApiResponse<Dto.PageResponse<Dto.TaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(Dto.ApiResponse.ok(taskService.getAllTasks(page, size)));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all registered users")
    public ResponseEntity<Dto.ApiResponse<List<Dto.UserResponse>>> getAllUsers() {
        List<Dto.UserResponse> users = userRepo.findAll()
                .stream()
                .map(Dto.UserResponse::from)
                .toList();
        return ResponseEntity.ok(Dto.ApiResponse.ok(users));
    }
}
