package com.edms.file_management.proxy;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter("/*") // Apply the filter to all incoming requests
public class ProxyFilter implements Filter {

    @Value("${proxy.secret}")
    private String proxySecret;

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check for the 'X-Proxy-Secret' header
        String proxySecretHeader = httpRequest.getHeader("X-Proxy-Secret");

        // Validate the proxy secret
        if (proxySecret == null || !proxySecret.equals(proxySecretHeader)) {
            // If the header is missing or invalid, reject the request
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Invalid Proxy Secret");
            return; // Stop further processing
        }

        // If valid, continue with the request chain
        chain.doFilter(request, response);
    }
}