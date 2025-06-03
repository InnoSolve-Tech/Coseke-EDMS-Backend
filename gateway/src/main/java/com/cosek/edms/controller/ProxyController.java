package com.cosek.edms.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
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

    @RequestMapping(value = {"/file-management/**", "/workflows/**", "/tasks/**", "/forms/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<String> proxyRequest(HttpServletRequest request, @RequestBody(required = false) String body) {
        try {
            // Extract HTTP method
            HttpMethod method;
            try {
                method = HttpMethod.valueOf(request.getMethod());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Unsupported HTTP method: " + request.getMethod());
            }

            // Construct backend URL
            String path = request.getRequestURI();
            String backendUrl = getBackendUrl(path);

            String queryString = request.getQueryString();
            if (queryString != null) {
                backendUrl += "?" + queryString;
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                headers.add(headerName, request.getHeader(headerName));
            });
            headers.set("X-Proxy-Secret", proxySecret);

            // Prepare forwarded request
            HttpEntity<String> forwardedRequest = new HttpEntity<>(body, headers);

            // Execute proxied request
            System.out.println("Forwarding request to: " + backendUrl);
            System.out.println("HTTP Method: " + method);
            System.out.println("Forwarded Request: " + forwardedRequest);
            return restTemplate.exchange(backendUrl, method, forwardedRequest, String.class);

        } catch (IllegalArgumentException e) {
            // Log known issues
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid proxy configuration: " + e.getMessage());
        } catch (Exception e) {
            // Log unexpected issues
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Proxy error: " + e.getMessage());
        }
    }

    private String getBackendUrl(String path) {
        if (path.startsWith("/file-management")) {
            return "http://file-management:8081" + path;
        } else if (path.startsWith("/workflows")) {
            return "http://workflows:8082" + path;
        } else if (path.startsWith("/tasks")) {
            return "http://tasks:8083" + path;
        }  else if (path.startsWith("/forms")) {
            return "http://forms:8084" + path;
        } else {
            throw new IllegalArgumentException("Unknown service path: " + path);
        }
    }
}