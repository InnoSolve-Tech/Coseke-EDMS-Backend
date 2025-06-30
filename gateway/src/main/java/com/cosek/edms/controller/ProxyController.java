package com.cosek.edms.controller;

import com.cosek.edms.config.ProxyRoutingProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    private final RestTemplate restTemplate;
    private final ProxyRoutingProperties routingProperties;

    public ProxyController(RestTemplate restTemplate, ProxyRoutingProperties routingProperties) {
        this.restTemplate = restTemplate;
        this.routingProperties = routingProperties;
    }

    @RequestMapping("/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request, @RequestBody(required = false) String body) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);

        log.info("=== PROXY REQUEST START ===");
        log.info("Client IP: {}", clientIp);
        log.info("Method: {}", method);
        log.info("Original Path: {}", path);
        log.info("Query String: {}", queryString != null ? queryString : "none");
        log.info("Content-Type: {}", request.getContentType());
        log.info("Content-Length: {}", request.getContentLength());

        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method);
            String backendUrl = getBackendUrl(path);

            if (queryString != null) {
                backendUrl += "?" + queryString;
            }

            log.info("Target URL: {}", backendUrl);

            HttpHeaders headers = buildHeaders(request);
            logHeaders(headers);

            HttpEntity<String> forwardedRequest = new HttpEntity<>(body, headers);

            if (body != null && !body.isEmpty()) {
                log.info("Request Body Length: {} chars", body.length());
                log.debug("Request Body: {}", body.length() > 1000 ? body.substring(0, 1000) + "..." : body);
            }

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(backendUrl, httpMethod, forwardedRequest, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("Response Status: {}", response.getStatusCode());
            log.info("Response Duration: {}ms", duration);
            log.info("Response Body Length: {} chars", response.getBody() != null ? response.getBody().length() : 0);

            // Log response headers
            if (!response.getHeaders().isEmpty()) {
                log.debug("Response Headers: {}",
                        response.getHeaders().entrySet().stream()
                                .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                                .collect(Collectors.joining("; ")));
            }

            log.info("=== PROXY REQUEST SUCCESS ===");
            return response;

        } catch (HttpClientErrorException e) {
            log.error("=== PROXY CLIENT ERROR ===");
            log.error("Status: {}", e.getStatusCode());
            log.error("Response Body: {}", e.getResponseBodyAsString());
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body("Client error: " + e.getMessage());

        } catch (HttpServerErrorException e) {
            log.error("=== PROXY SERVER ERROR ===");
            log.error("Status: {}", e.getStatusCode());
            log.error("Response Body: {}", e.getResponseBodyAsString());
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body("Server error: " + e.getMessage());

        } catch (ResourceAccessException e) {
            log.error("=== PROXY CONNECTION ERROR ===");
            log.error("Cannot connect to backend service");
            log.error("Error: {}", e.getMessage());
            log.error("Cause: {}", e.getCause() != null ? e.getCause().getMessage() : "Unknown");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Cannot connect to backend service: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            log.error("=== PROXY CONFIG ERROR ===");
            log.error("Invalid configuration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid proxy configuration: " + e.getMessage());

        } catch (Exception e) {
            log.error("=== PROXY UNEXPECTED ERROR ===");
            log.error("Unexpected error occurred", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Proxy error: " + e.getMessage());
        }
    }

    private String getBackendUrl(String path) {
        log.debug("Resolving backend URL for path: {}", path);

        String[] parts = path.split("/");
        if (parts.length < 2) {
            log.error("Invalid path structure: {}", path);
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        String serviceKey = parts[1];
        log.debug("Service key extracted: {}", serviceKey);

        String baseUrl = routingProperties.getRoutes().get(serviceKey);
        if (baseUrl == null) {
            log.error("No route configured for service: {}", serviceKey);
            log.debug("Available routes: {}", routingProperties.getRoutes().keySet());
            throw new IllegalArgumentException("No route defined for service: " + serviceKey);
        }

        String finalUrl = baseUrl + path;
        log.debug("Final backend URL: {}", finalUrl);
        return finalUrl;
    }

    private HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            // Skip headers that might cause issues
            if (!headerName.equalsIgnoreCase("host") &&
                    !headerName.equalsIgnoreCase("content-length")) {
                headers.add(headerName, headerValue);
            }
        });
        headers.set("X-Proxy-Secret", routingProperties.getSecret());
        headers.set("X-Forwarded-For", getClientIp(request));
        headers.set("X-Forwarded-Proto", request.getScheme());
        headers.set("X-Forwarded-Host", request.getServerName());
        return headers;
    }

    private void logHeaders(HttpHeaders headers) {
        if (log.isDebugEnabled()) {
            log.debug("Request Headers Count: {}", headers.size());
            headers.forEach((name, values) -> {
                if (!name.equalsIgnoreCase("authorization") &&
                        !name.equalsIgnoreCase("x-proxy-secret")) {
                    log.debug("Header: {} = {}", name, String.join(",", values));
                } else {
                    log.debug("Header: {} = [REDACTED]", name);
                }
            });
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}