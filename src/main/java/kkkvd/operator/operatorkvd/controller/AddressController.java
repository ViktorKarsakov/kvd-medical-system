package kkkvd.operator.operatorkvd.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

//Прокси-контроллер для подсказок адресов через DaData API
@RestController
@RequestMapping("/api/address")
public class AddressController {
    @Value("${dadata.api.token}")
    private String dadataToken;

    private static final String DADATA_SUGGEST_URL = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address";

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/suggest")
    public ResponseEntity<String> suggestAddress(@RequestBody Map<String, Object> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Token " + dadataToken);
        //Максимум 5 подсказок (если фронт не передал count)
        request.putIfAbsent("count", 5);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    DADATA_SUGGEST_URL, HttpMethod.POST, entity, String.class
            );
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"suggestions\":[]}");
        }
    }
}
