package com.khateeb.controller;

import com.khateeb.entity.UserEntity;
import com.khateeb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class GoogleDocsController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createGoogleDoc(@RequestBody Map<String, String> requestPayload,
            Authentication authentication) {
        String email = ((DefaultOAuth2User) authentication.getPrincipal()).getAttribute("email");
        String title = requestPayload.getOrDefault("title", "Untitled Document");

        // Retrieve user access token
        UserEntity user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String accessToken = user.getAccessToken();

        // Call Google Docs API
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://docs.googleapis.com/v1/documents";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("title", title);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create Google Doc", "details", e.getMessage()));
        }
    }
}
