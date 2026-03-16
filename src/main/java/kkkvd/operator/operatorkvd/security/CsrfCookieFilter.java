package kkkvd.operator.operatorkvd.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//CsrfCookieFilter — принудительно загружает CSRF-токен при каждом запросе
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        //Достаём CsrfToken из атрибутов запроса
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (token != null) {
            // Spring Security генерирует значение токена и записывает cookie.
            // Без этого вызова cookie не появится до первого POST.
            token.getToken();
        }

        //Передаём запрос дальше по цепочке фильтров
        filterChain.doFilter(request, response);
    }
}
