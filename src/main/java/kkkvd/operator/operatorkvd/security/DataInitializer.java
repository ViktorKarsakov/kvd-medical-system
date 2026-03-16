package kkkvd.operator.operatorkvd.security;

import kkkvd.operator.operatorkvd.entities.Role;
import kkkvd.operator.operatorkvd.entities.User;
import kkkvd.operator.operatorkvd.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//DataInitializer — создаёт учётку администратора при первом запуске приложения
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //Вызывается автоматически при старте Spring Boot
    @Override
    public void run(String... args) {
        //Проверяем, существует ли уже учётка admin
        if (userRepository.existsByUsername("admin")) {
            //Админ существует - выходим
            return;
        }

        //Создаем учетку админа
        User admin = new User();
        admin.setUsername("admin");
        admin.setFullName("Администратор");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setPassword(passwordEncoder.encode("236Sync75df"));
        userRepository.save(admin);

        System.out.println("══════════════════════════════════════════════════");
        System.out.println("  Создана учётная запись администратора:");
        System.out.println("══════════════════════════════════════════════════");
    }
}
