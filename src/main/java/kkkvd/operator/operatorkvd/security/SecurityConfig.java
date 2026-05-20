package kkkvd.operator.operatorkvd.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;


//SecurityConfig — ГЛАВНЫЙ файл настройки безопасности приложения
//1) Какие страницы доступны без входа (login, CSS, JS)
//2) Какие страницы требуют аутентификации (все остальные)
//3) Как выглядит страница входа
//4) Как хэшировать пароли (BCrypt)
//5) Как защититься от CSRF-атак
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //1) ПРАВИЛА ДОСТУПА: кто куда может ходить
                .authorizeHttpRequests(auth -> auth
                        //Страница входа и её ресурсы — доступны ВСЕМ (иначе не войти)
                        .requestMatchers("/login", "/login.html").permitAll()
                        // CSS, JS, шрифты — доступны ВСЕМ (нужны для отрисовки login-страницы)
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // Управление пользователями — только ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        //Журнал действий доступен только ADMIN
                        .requestMatchers("/api/audit-log/**").hasRole("ADMIN")
                        //Бэкап БД
                        .requestMatchers("/api/backup/**").hasRole("ADMIN")
                        //Справочники: чтение — все авторизованные, изменение — только ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/dictionaries/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/dictionaries/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/dictionaries/**").hasRole("ADMIN")
                        // Все остальные запросы — только для авторизованных пользователей
                        .anyRequest().authenticated()
                )
                //2) ФОРМА ВХОДА
                .formLogin(form -> form
                        // Адрес страницы входа (HTML-файл). Если неавторизованный пользователь пытается открыть любую страницу, Spring Security перенаправит его сюда.
                        .loginPage("/login.html")
                        //URL, на который HTML-форма отправляет POST с username и password
                        .loginProcessingUrl("/login")
                        //Перенаправляет после успешного входа
                        .defaultSuccessUrl("/patient-form.html", false)
                        //Имена полей в HTML-форме (по умолчанию "username" и "password")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        //Разрешаем доступ к login-эндпоинтам всем
                        .permitAll()
                )
                //3) ВЫХОД ИЗ СИСТЕМЫ
                .logout(logout -> logout
                        //URL для выхода
                        .logoutUrl("/logout")
                        //Перенаправляет после выхода
                        .logoutSuccessUrl("/login.html?logout")
                        //Удалить сессию и cookie при выходе
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                //4) CSRF-ЗАЩИТА
                .csrf(csrf -> csrf
                        //CookieCsrfTokenRepository — Spring Security ставит CSRF-токен в cookie с именем "XSRF-TOKEN". withHttpOnlyFalse() — JavaScript может прочитать эту cookie. (По умолчанию cookie httpOnly=true → JS не видит её.)
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        //CsrfTokenRequestAttributeHandler — использует «простой» токен (не XOR-encoded)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                //Добавляем наш CsrfCookieFilter ПОСЛЕ стандартного CsrfFilter. Он принудительно «материализует» CSRF-токен → cookie появляется сразу
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                //5) ОБРАБОТКА ОШИБОК ДЛЯ AJAX-ЗАПРОСОВ
                //для AJAX-запросов (заголовок X-Requested-With: XMLHttpRequest или Accept: application/json) возвращаем 401 вместо редиректа
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            //Проверяем, это AJAX-запрос или обычный переход по ссылке
                            String accept = request.getHeader("Accept");
                            String xRequested = request.getHeader("X-Requested-With");
                            boolean isAjax = "XMLHttpRequest".equals(xRequested) || (accept != null && accept.contains("application/json"));

                            if (isAjax) {
                                //AJAX-запрос → возвращаем 401 (JavaScript обработает)
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\": \"Unauthorized\"}");
                            } else {
                                response.sendRedirect("/login.html");
                            }
                        })
                );
                return http.build();
    }

    //PasswordEncoder — алгоритм хэширования паролей
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
