package kkkvd.operator.operatorkvd.service;

import kkkvd.operator.operatorkvd.dto.CreateUserRequest;
import kkkvd.operator.operatorkvd.entities.User;
import kkkvd.operator.operatorkvd.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

//UserService — бизнес-логика управления пользователями, сервис доступен только администратору
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //Получить список всех пользователей
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    //Создать нового пользователя
    public User createUser(CreateUserRequest request){
        //проверяем уникальность
        if (userRepository.existsByUsername(request.getUsername())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с логином '" + request.getUsername() + "' уже существует");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setEnabled(true);
        //Хэшируем пароль
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    //Заблокировать или разблокировать пользователя
    public User toggleEnabled(Long userId, boolean enabled, String currentUsername){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        // Защита: нельзя отключить самого себя
        if (!enabled && user.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя заблокировать самого себя");
        }
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    //Сбросить пароль пользователя (админом)
    public User resetPassword(Long userId, String newPassword){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    //Смена пароля самим пользователем (не админом)
    public void changeOwnPassword(String username, String oldPassword, String newPassword){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        //Проверяем, что старый пароль правильный
        if(!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный текущий пароль");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
