package com.cosek.edms.controller;

import com.cosek.edms.config.ProxyRoutingProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "*")
public class ProxyController {

    private final RestTemplate restTemplate;
    private final ProxyRoutingProperties routingProperties;

    public ProxyController(RestTemplate restTemplate, ProxyRoutingProperties routingProperties) {
        this.restTemplate = restTemplate;
        this.routingProperties = routingProperties;
    }

    @RequestMapping("/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request, @RequestBody(required = false) String body) {
        try {
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            String path = request.getRequestURI();
            String backendUrl = getBackendUrl(path);

            String queryString = request.getQueryString();
            if (queryString != null) {
                backendUrl += "?" + queryString;
            }

            HttpHeaders headers = new HttpHeaders();
            request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                    headers.add(headerName, request.getHeader(headerName))
            );
            headers.set("X-Proxy-Secret", routingProperties.getSecret());

            HttpEntity<String> forwardedRequest = new HttpEntity<>(body, headers);
            return restTemplate.exchange(backendUrl, method, forwardedRequest, String.class);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid proxy configuration: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Proxy error: " + e.getMessage());
        }
    }

    private String getBackendUrl(String path) {
        String[] parts = path.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        String serviceKey = parts[1];
        String baseUrl = routingProperties.getRoutes().get(serviceKey);
        if (baseUrl == null) {
            throw new IllegalArgumentException("No route defined for service: " + serviceKey);
        }
        return baseUrl + path;
    }
}
