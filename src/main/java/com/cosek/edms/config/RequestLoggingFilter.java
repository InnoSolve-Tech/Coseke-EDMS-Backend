package com.cosek.edms.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String username = (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User)
                ? ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername()
                : "anonymous";

        logger.info("Request URL: {}", httpServletRequest.getRequestURL());
        logger.info("Request Method: {}", httpServletRequest.getMethod());
        logger.info("Request IP: {}", httpServletRequest.getRemoteAddr());
        logger.info("Authenticated User: {}", username);

        // Proceed with the next filter in the chain
        chain.doFilter(request, response);
    }
}
