/*
 * This controller interacts with Google Docs API using the user's OAuth2 access token. 
 * It allows the authenticated user to create Google Docs by leveraging their access token, 
 * which was fetched during the OAuth2 login flow.
 */

package com.khateeb.controller;

import com.khateeb.entity.UserEntity;
import com.khateeb.service.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/docs")
@RequiredArgsConstructor
public class GoogleDocsController {

    private final UserService userService;

    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/create")
    public ResponseEntity<?> createGoogleDoc(@RequestBody Map<String, String> requestPayload,
            Authentication authentication) {
        String email = ((DefaultOAuth2User) authentication.getPrincipal()).getAttribute("email");
        String title = requestPayload.getOrDefault("title", "Untitled Document");

        // Get a valid access token (refresh if necessary)
        String accessToken = googleOAuthService.getValidAccessToken(email);

        // Debugging
        System.out.println("--------------------------");
        System.out.println(accessToken);

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
