/*
 * This controller provides endpoints for fetching user details. 
 * It uses OAuth2User to extract and return the authenticated user's information 
 * (like email and name). It could also provide additional information, such as 
 * the user's access token, if needed.
 */

package com.khateeb.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Autowired
    public AuthController(OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @GetMapping("/user")
    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal,
            Authentication authentication,
            HttpServletRequest request) {
        if (principal == null) {
            throw new RuntimeException("User is not authenticated");
        }

        // Fetch the OAuth2AuthorizedClient from the repository
        OAuth2AuthorizedClient client = authorizedClientRepository.loadAuthorizedClient(
                "google",
                authentication,
                request);

        if (client == null) {
            throw new RuntimeException("OAuth2AuthorizedClient not found");
        }

        String accessToken = client.getAccessToken().getTokenValue();
        // System.out.println("Access Token: " + accessToken);

        Map<String, Object> response = new HashMap<>();
        response.put("user", principal.getAttributes());
        response.put("accessToken", accessToken);
        return response;
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to the application!";
    }

    @GetMapping("/home")
    public String home() {
        return "Welcome to your home page!";
    }
}
