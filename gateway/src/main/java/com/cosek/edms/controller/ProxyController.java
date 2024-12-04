package com.cosek.edms.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "*")
public class ProxyController {

    private final RestTemplate restTemplate;

    @Value("${proxy.secret}")
    private String proxySecret;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<String> proxyRequest(HttpServletRequest request, HttpEntity<String> httpEntity) {
        try {
            // Forward the request method
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            // Construct the backend URL
            String path = request.getRequestURI();
            String backendUrl = getBackendUrl(path);

            String queryString = request.getQueryString();
            if (queryString != null) {
                backendUrl += "?" + queryString;
            }

            // Include headers and body from the incoming request
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(httpEntity.getHeaders()); // Copy all headers from incoming request
            headers.set("X-Proxy-Secret", proxySecret); // Add custom proxy secret header if needed

            HttpEntity<String> forwardedRequest = new HttpEntity<>(httpEntity.getBody(), headers);

            // Send the proxied request
            return restTemplate.exchange(backendUrl, method, forwardedRequest, String.class);
        } catch (Exception e) {
            // Log the full stack trace for debugging
            e.printStackTrace();
            // Log and handle the error
            return ResponseEntity.status(500).body("Proxy error: " + e.getMessage());
        }
    }

    private String getBackendUrl(String path) {
        // Use the exact service names from docker-compose.yml
        if (path.startsWith("/file-management")) {
            return "http://file-management:8081" + path;
        } else if (path.startsWith("/workflows")) {
            return "http://workflows:8082" + path;
        } else {
            throw new IllegalArgumentException("Unknown service");
        }
    }
}
