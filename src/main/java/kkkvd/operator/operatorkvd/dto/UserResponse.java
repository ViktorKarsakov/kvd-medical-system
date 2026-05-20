package kkkvd.operator.operatorkvd.dto;


import kkkvd.operator.operatorkvd.entities.Role;
import kkkvd.operator.operatorkvd.entities.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

//UserResponse — данные о пользователе, которые возвращаем клиенту
@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;

    //Фабричный метод: создаёт UserResponse из User entity
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
