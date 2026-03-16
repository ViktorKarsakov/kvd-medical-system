package kkkvd.operator.operatorkvd.controller;

import kkkvd.operator.operatorkvd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//AuthController — API для работы с аутентификацией
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    //Возвращает информацию о текущем авторизованном пользователе
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        // Получаем username из объекта Authentication
        String username = authentication.getName();
        // Получаем роль. Одна роль на пользователя — берём первую
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .findFirst()
                .orElse("UNKNOWN");
        return ResponseEntity.ok(Map.of("username", username, "role", role));
    }

    //Смена собственного пароля (доступна любому авторизованному пользователю)
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> body, Authentication authentication) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        userService.changeOwnPassword(authentication.getName(), oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменён"));
    }
}
