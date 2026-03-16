package kkkvd.operator.operatorkvd.controller;

import jakarta.validation.Valid;
import kkkvd.operator.operatorkvd.dto.CreateUserRequest;
import kkkvd.operator.operatorkvd.dto.UserResponse;
import kkkvd.operator.operatorkvd.entities.User;
import kkkvd.operator.operatorkvd.service.AuditLogService;
import kkkvd.operator.operatorkvd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//UserController — API для управления пользователями системы
//Доступ только для пользователей с ролью ADMIN
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuditLogService auditLogService;

    //Получить список всех пользователей
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> result = userService.getAllUsers().stream().map(UserResponse::fromEntity).toList();
        return ResponseEntity.ok(result);
    }

    //Создать нового пользователя
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        User created = userService.createUser(request);
        auditLogService.log("CREATE", "USER", created.getId(),
                "Создан пользователь: " + created.getUsername() + " (" + created.getFullName() + ")", authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(created));
    }

    //Заблокировать или разблокировать пользователя
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<UserResponse> toggleUser(@PathVariable Long id, @RequestBody Map<String, Boolean> body, Authentication authentication) {
        boolean enabled = body.getOrDefault("enabled", true);
        User updated = userService.toggleEnabled(id, enabled, authentication.getName());

        auditLogService.log("UPDATE", "USER", id,
                (enabled ? "Разблокирован" : "Заблокирован") + " пользователь: " + updated.getUsername(),
                authentication.getName());

        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }

    //Сбросить пароль пользователя
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<UserResponse> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication authentication) {
        String newPassword = body.get("newPassword");
        User updated = userService.resetPassword(id, newPassword);

        auditLogService.log("UPDATE", "USER", id,
                "Сброшен пароль пользователю: " + updated.getUsername(),
                authentication.getName());

        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }
}
