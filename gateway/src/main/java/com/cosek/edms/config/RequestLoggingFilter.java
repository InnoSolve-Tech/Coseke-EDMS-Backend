package com.cosek.edms.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends GenericFilterBean {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        // Get authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "anonymous";

        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
        }

        logger.info("Request URL: {}", httpServletRequest.getRequestURL());
        logger.info("Request Method: {}", httpServletRequest.getMethod());
        logger.info("Request IP: {}", httpServletRequest.getRemoteAddr());
        logger.info("Authenticated User: {}", username);

        // Proceed with the next filter in the chain
        chain.doFilter(request, response);
    }
}
