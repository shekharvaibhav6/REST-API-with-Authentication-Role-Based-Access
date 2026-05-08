package com.example.roleauthentication.service;
import com.example.roleauthentication.dto.Dto;
import com.example.roleauthentication.entity.Task;
import com.example.roleauthentication.entity.User;
import com.example.roleauthentication.exception.AppException;
import com.example.roleauthentication.exception.ResourceNotFoundException;
import com.example.roleauthentication.repository.TaskRepository;
import com.example.roleauthentication.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public TaskService(TaskRepository taskRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    private User fetchUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found"));
    }

    public Dto.PageResponse<Dto.TaskResponse> getMyTasks(String email, int page, int size) {
        User user = fetchUser(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> result = taskRepo.findByUserId(user.getId(), pageable);

        return new Dto.PageResponse<>(
                result.getContent().stream().map(Dto.TaskResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    public Dto.TaskResponse getTask(Long taskId, String email) {
        User user = fetchUser(email);
        Task task = taskRepo.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        return Dto.TaskResponse.from(task);
    }

    @Transactional
    public Dto.TaskResponse createTask(Dto.TaskRequest req, String email) {
        User user = fetchUser(email);

        Task task = new Task();
        task.setTitle(req.title().trim());
        task.setDescription(req.description() != null ? req.description().trim() : null);
        task.setStatus(req.status() != null ? req.status() : Task.Status.PENDING);
        task.setPriority(req.priority() != null ? req.priority() : Task.Priority.MEDIUM);
        task.setUser(user);

        return Dto.TaskResponse.from(taskRepo.save(task));
    }

    @Transactional
    public Dto.TaskResponse updateTask(Long taskId, Dto.TaskRequest req, String email) {
        User user = fetchUser(email);
        Task task = taskRepo.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        task.setTitle(req.title().trim());
        task.setDescription(req.description() != null ? req.description().trim() : null);
        if (req.status() != null) task.setStatus(req.status());
        if (req.priority() != null) task.setPriority(req.priority());

        return Dto.TaskResponse.from(taskRepo.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId, String email) {
        User user = fetchUser(email);
        Task task = taskRepo.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        taskRepo.delete(task);
    }

    // Admin-only: see all tasks
    public Dto.PageResponse<Dto.TaskResponse> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> result = taskRepo.findAll(pageable);

        return new Dto.PageResponse<>(
                result.getContent().stream().map(Dto.TaskResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}