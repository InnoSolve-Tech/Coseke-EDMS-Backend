package com.cosek.edms.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Enumeration;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final RestTemplate restTemplate;

    @Value("${proxy.secret}")
    private String proxySecret;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        logger.info("Received {} request for path: {}", method, path);

        try {
            // Get the request body if present
            String requestBody = request.getReader().lines().collect(Collectors.joining());

            // Get backend URL
            String backendUrl = getBackendUrl(path);
            String queryString = request.getQueryString();
            if (queryString != null) {
                backendUrl += "?" + queryString;
            }

            logger.debug("Forwarding to backend URL: {}", backendUrl);

            // Copy original headers
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!"host".equalsIgnoreCase(headerName)) {
                    headers.set(headerName, request.getHeader(headerName));
                }
            }

            // Add the proxy secret
            headers.set("X-Proxy-Secret", proxySecret);

            // Create http entity
            HttpEntity<String> entity = new HttpEntity<>(
                    requestBody.isEmpty() ? null : requestBody,
                    headers
            );

            logger.debug("Request body: {}", requestBody);

            // Forward the request
            ResponseEntity<String> response = restTemplate.exchange(
                    backendUrl,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
            );

            logger.info("Successfully proxied {} request to {}", method, path);
            return response;

        } catch (HttpStatusCodeException e) {
            logger.error("Error proxying request to {}: {} - {}",
                    path, e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error proxying request to " + path, e);
            throw e;
        }
    }

    private String getBackendUrl(String path) {
        // Determine which service to forward the request to based on the path
        if (path.startsWith("/file-management")) {
            return "http://host.docker.internal:8081" + path;
        } else if (path.startsWith("/workflows")) {
            return "http://host.docker.internal:8082" + path;
        } else {
            String message = "Unknown service path: " + path;
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
    }
}