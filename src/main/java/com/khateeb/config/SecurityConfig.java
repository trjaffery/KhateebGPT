/*
 * This configuration class sets up Spring Security, enabling OAuth2 login, 
 * configuring HTTP security (like CSRF, CORS), and defining how authentication 
 * should be handled. It also wires up the OAuth2LoginSuccessHandler for 
 * handling post-login logic.
 */

package com.khateeb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        @Lazy
        private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // NEED TO CHANGE FOR PRODUCTION
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/home").permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> {
                                        oauth2.successHandler(oAuth2LoginSuccessHandler);
                                });
                return http.build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
                configuration.addAllowedHeader("*");
                configuration.addAllowedMethod("*");
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
                urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
                return urlBasedCorsConfigurationSource;
        }

        @Bean
        public OAuth2AuthorizedClientRepository authorizedClientRepository() {
                return new HttpSessionOAuth2AuthorizedClientRepository();
        }

}
