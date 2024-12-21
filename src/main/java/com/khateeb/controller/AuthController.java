/*
 * This controller provides endpoints for fetching user details. 
 * It uses OAuth2User to extract and return the authenticated user's information 
 * (like email and name). It could also provide additional information, such as 
 * the user's access token, if needed.
 */

package com.khateeb.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.khateeb.service.*;
import com.khateeb.entity.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final GoogleAuthorizationCodeFlow flow;

    @GetMapping("/user")
    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
    public Map<String, Object> getUser(@RequestParam("code") String code) throws IOException {
        // Exchange the authorization code for tokens
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        Credential credential = flow.createAndStoreCredential(tokenResponse, "userId");

        // Fetch user details from Google
        Map<String, Object> userDetails = fetchUserDetails(credential.getAccessToken());

        // Save user info to the database
        saveUser("userId", userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("user", userDetails);
        response.put("accessToken", credential.getAccessToken());
        return response;
    }

    private Map<String, Object> fetchUserDetails(String accessToken) throws IOException {
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(
                (HttpRequest request) -> request.getHeaders().setAuthorization("Bearer " + accessToken));

        HttpRequest request = requestFactory
                .buildGetRequest(new GenericUrl("https://www.googleapis.com/oauth2/v1/userinfo"));
        HttpResponse response = request.execute();
        return new ObjectMapper().readValue(response.getContent(), Map.class);
    }

    private void saveUser(String userId, Map<String, Object> userDetails) {
        userService.findByEmail(userDetails.get("email").toString()).ifPresentOrElse(
                user -> userService.save(user),
                () -> {
                    UserEntity newUser = new UserEntity();
                    // newUser.setId(userId);
                    newUser.setEmail(userDetails.get("email").toString());
                    newUser.setName(userDetails.get("name").toString());
                    userService.save(newUser);
                });
    }
}
