package com.cosek.edms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from the Authorization header
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUserEmail(jwt); // Extract the user email from the JWT

        // Validate the email and check if the user is not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details using the UserDetailsService
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validate the JWT token with the loaded user details
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // If the token is valid, create a UsernamePasswordAuthenticationToken
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials are not needed as they are already authenticated
                        userDetails.getAuthorities() // User's authorities are set here
                );

                // Set the details of the authentication request (remote address, session ID, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Proceed with the next filter in the chain
        filterChain.doFilter(request, response);
    }
}
