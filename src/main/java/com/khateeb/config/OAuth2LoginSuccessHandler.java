/*
 * this is triggered when a user successfully logs in
 * extracts users info and redirects to frontend
 */

package com.khateeb.config;

import com.khateeb.entity.UserEntity;
import com.khateeb.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor // Automatically generates a constructor for final fields
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserService userService;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

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

        // Extract OAuth2 token
        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId(),
                oAuth2AuthenticationToken.getName());
        // String accessToken = client.getAccessToken().getTokenValue();

        // Debug log
        System.out.println("User authenticated: " + email + ", " + name);
        // System.out.println("access token: " + accessToken);

        // Save user to database or find existing user
        userService.findByEmail(email).ifPresentOrElse(
                user -> {
                    // user.setAccessToken(accessToken); // Save token for API access
                    userService.save(user);
                },
                () -> {
                    // New user: Save to database
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    // newUser.setAccessToken(accessToken); // Save token for API access
                    userService.save(newUser);
                });

        // Redirect to frontend after successful login
        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(frontendUrl);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
