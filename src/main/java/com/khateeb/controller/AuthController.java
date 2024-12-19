// Provides simple endpoints to return user information and static content.

package com.khateeb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    // Constructor injection ensures that this service is initialized
    @Autowired
    public AuthController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            throw new RuntimeException("User is not authenticated");
        }

        // OAuth2AuthorizedClient client = authorizedClientService
        // .loadAuthorizedClient("google", principal.getName());

        // if (client == null) {
        // throw new RuntimeException("OAuth2AuthorizedClient not found");
        // }

        // String accessToken = client.getAccessToken().getTokenValue();
        // System.out.println("Access Token: " + accessToken);

        Map<String, Object> response = new HashMap<>();
        response.put("user", principal.getAttributes());
        // response.put("accessToken", accessToken); // Include access token
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
