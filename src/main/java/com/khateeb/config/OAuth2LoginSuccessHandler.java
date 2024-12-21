/*
 * This component is triggered after a user successfully logs in via OAuth2 
 * It captures the user’s OAuth2 authentication details (like email and name), 
 * checks if the user already exists in the database, and saves or updates the 
 * user in the UserEntity repository. The handler then redirects the user to the 
 * frontend page after successful authentication.
 */

package com.khateeb.config;

import com.khateeb.entity.UserEntity;
import com.khateeb.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserService userService;

    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        // Extract user details from OAuth2 principal
        DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();
        String email = attributes.getOrDefault("email", "").toString();
        String name = attributes.getOrDefault("name", "").toString();

        // Fetch OAuth2AuthorizedClient
        OAuth2AuthorizedClient client = authorizedClientRepository.loadAuthorizedClient(
                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId(),
                authentication,
                request);

        if (client == null) {
            System.out.println("OAuth2AuthorizedClient is null");
        } else {
            String accessToken = client.getAccessToken().getTokenValue();
            String refreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;

            // Debug log
            System.out.println("Access Token: " + accessToken);
            System.out.println("Refresh Token: " + (refreshToken != null ? refreshToken : "No refresh token provided"));

            // Save user to database or update existing user
            userService.findByEmail(email).ifPresentOrElse(
                    user -> {
                        user.setAccessToken(accessToken); // Save access token
                        if (refreshToken != null) {
                            user.setRefreshToken(refreshToken); // Save refresh token if available
                        }
                        userService.save(user);
                    },
                    () -> {
                        // New user: Save to database
                        UserEntity newUser = new UserEntity();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setAccessToken(accessToken); // Save access token
                        if (refreshToken != null) {
                            newUser.setRefreshToken(refreshToken); // Save refresh token if available
                        }
                        userService.save(newUser);
                    });
        }

        // Redirect to frontend after successful login
        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(frontendUrl);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
