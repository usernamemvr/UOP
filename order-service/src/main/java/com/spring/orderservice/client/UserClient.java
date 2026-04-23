package com.spring.orderservice.client;

import com.spring.orderservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_BASE_URL = "http://user-service:8080/users";
    // can be injected as ENV's from IntelliJ IDE

    public UserDto getUserById(Long id) {
        String url = USER_SERVICE_BASE_URL + "/" + id;
        return restTemplate.getForObject(url, UserDto.class);
    }
}

