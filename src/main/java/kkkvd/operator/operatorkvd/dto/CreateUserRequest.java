package kkkvd.operator.operatorkvd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kkkvd.operator.operatorkvd.entities.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Логин обязателен")
    private String username;
    @NotBlank(message = "Пароль обязателен")
    private String password;
    @NotBlank(message = "ФИО обязательно")
    private String fullName;
    @NotNull(message = "Роль обязательна")
    private Role role;
}
