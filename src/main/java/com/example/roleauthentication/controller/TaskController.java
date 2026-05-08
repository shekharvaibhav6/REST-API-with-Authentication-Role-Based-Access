package com.example.roleauthentication.controller;
import com.example.roleauthentication.dto.Dto;
import com.example.roleauthentication.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "CRUD operations on tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all my tasks (paginated)")
    public ResponseEntity<Dto.ApiResponse<Dto.PageResponse<Dto.TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(Dto.ApiResponse.ok(taskService.getMyTasks(email, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<Dto.ApiResponse<Dto.TaskResponse>> getTask(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {

        return ResponseEntity.ok(Dto.ApiResponse.ok(taskService.getTask(id, email)));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<Dto.ApiResponse<Dto.TaskResponse>> createTask(
            @Valid @RequestBody Dto.TaskRequest req,
            @AuthenticationPrincipal String email) {

        Dto.TaskResponse created = taskService.createTask(req, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Dto.ApiResponse.ok("Task created", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<Dto.ApiResponse<Dto.TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Dto.TaskRequest req,
            @AuthenticationPrincipal String email) {

        return ResponseEntity.ok(Dto.ApiResponse.ok("Task updated", taskService.updateTask(id, req, email)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Dto.ApiResponse<Void>> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {

        taskService.deleteTask(id, email);
        return ResponseEntity.ok(Dto.ApiResponse.ok("Task deleted", null));
    }
}
