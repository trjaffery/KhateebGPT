package com.khateeb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.khateeb.entity.*;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final UserService userService;

    public String refreshAccessToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            String newAccessToken = responseBody != null ? (String) responseBody.get("access_token") : null;

            if (newAccessToken != null) {
                return newAccessToken;
            } else {
                throw new RuntimeException("Failed to refresh access token: No token returned");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh access token: " + e.getMessage());
        }
    }

    public String getValidAccessToken(String email) {
        UserEntity user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = user.getAccessToken();
        String refreshToken = user.getRefreshToken();

        // Check if the access token is valid (e.g., try an API call or check expiration
        // time)
        try {
            // Test token validity by making a dummy request
            validateAccessToken(accessToken);
            return accessToken;
        } catch (Exception e) {
            // Access token is invalid; refresh it
            String newAccessToken = refreshAccessToken(refreshToken);

            // Save the new access token
            user.setAccessToken(newAccessToken);
            userService.save(user);

            return newAccessToken;
        }
    }

    private void validateAccessToken(String accessToken) {
        // Dummy request to verify if the access token is valid
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(apiUrl, HttpMethod.GET, request, Map.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Access token is invalid or expired");
            }
            throw e;
        }
    }
}
