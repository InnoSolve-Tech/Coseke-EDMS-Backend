package com.cosek.edms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.cosek.edms.helper.Constants.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RequestLoggingFilter requestLoggingFilter;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins (you might want to restrict this in production)
        // For production, replace with specific domains:
        // configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://yourdomain.com"));
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (if needed for JWT tokens)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Expose headers that might be needed by frontend
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Total-Count", "X-Forwarded-For"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .authorizeHttpRequests(request ->
                        request.requestMatchers(AUTH_ROUTE)
                                .permitAll()
                                // User routes
                                .requestMatchers(HttpMethod.POST, USER_ROUTE).hasAuthority(CREATE_USER)
                                .requestMatchers(HttpMethod.GET, USER_ROUTE).hasAuthority(READ_USER)
                                .requestMatchers(HttpMethod.DELETE, USER_ROUTE).hasAuthority(DELETE_USER)
                                .requestMatchers(HttpMethod.PUT, USER_ROUTE).hasAuthority(UPDATE_USER)
                                // Role routes
                                .requestMatchers(HttpMethod.POST, ROLE_ROUTE).hasAuthority(CREATE_ROLE)
                                .requestMatchers(HttpMethod.GET, ROLE_ROUTE).hasAuthority(READ_ROLE)
                                .requestMatchers(HttpMethod.DELETE, ROLE_ROUTE).hasAuthority(DELETE_ROLE)
                                .requestMatchers(HttpMethod.PUT, ROLE_ROUTE).hasAuthority(UPDATE_ROLE)
                                // Permission routes
                                .requestMatchers(HttpMethod.POST, PERMISSION_ROUTE).hasAuthority(CREATE_PERMISSION)
                                .requestMatchers(HttpMethod.GET, PERMISSION_ROUTE).hasAuthority(READ_PERMISSION)
                                .requestMatchers(HttpMethod.DELETE, PERMISSION_ROUTE).hasAuthority(DELETE_PERMISSION)
                                .requestMatchers(HttpMethod.PUT, PERMISSION_ROUTE).hasAuthority(UPDATE_PERMISSION)
                                // Workflow routes
                                .requestMatchers(HttpMethod.POST, WORKFLOW_ROUTE).hasAuthority(CREATE_WORKFLOW)
                                .requestMatchers(HttpMethod.GET, WORKFLOW_ROUTE).hasAuthority(READ_WORKFLOW)
                                .requestMatchers(HttpMethod.DELETE, WORKFLOW_ROUTE).hasAuthority(DELETE_WORKFLOW)
                                .requestMatchers(HttpMethod.PUT, WORKFLOW_ROUTE).hasAuthority(UPDATE_WORKFLOW)
                                // Form routes
                                .requestMatchers(HttpMethod.POST, FORM_ROUTE).hasAuthority(CREATE_FORM)
                                .requestMatchers(HttpMethod.GET, FORM_ROUTE).hasAuthority(READ_FORM)
                                .requestMatchers(HttpMethod.DELETE, FORM_ROUTE).hasAuthority(DELETE_FORM)
                                .requestMatchers(HttpMethod.PUT, FORM_ROUTE).hasAuthority(UPDATE_FORM)
                                // Log routes
                                .requestMatchers(HttpMethod.POST, LOG_ROUTE).hasAuthority(CREATE_LOG)
                                .requestMatchers(HttpMethod.GET, LOG_ROUTE).hasAuthority(READ_LOG)
                                .requestMatchers(HttpMethod.DELETE, LOG_ROUTE).hasAuthority(DELETE_LOG)
                                .requestMatchers(HttpMethod.PUT, LOG_ROUTE).hasAuthority(UPDATE_LOG)
                                // Folder routes
                                .requestMatchers(HttpMethod.POST, FILES_MANAGEMENT_ROUTE).hasAuthority(CREATE_FOLDER)
                                .requestMatchers(HttpMethod.DELETE, FILES_MANAGEMENT_ROUTE).hasAuthority(DELETE_FOLDER)
                                .requestMatchers(HttpMethod.PUT, FILES_MANAGEMENT_ROUTE).hasAuthority(UPDATE_FOLDER)
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}