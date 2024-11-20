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

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String backendUrl = getBackendUrl(path);

        String queryString = request.getQueryString();
        if (queryString != null) {
            backendUrl += "?" + queryString;
        }

        // Set up headers with the proxy secret
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Proxy-Secret", proxySecret);

        // Forward the request to the backend service with the headers
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(backendUrl, HttpMethod.GET, entity, String.class);
    }

    private String getBackendUrl(String path) {
        // Determine which service to forward the request to based on the path
        if (path.startsWith("/file-management")) {
            return "http://host.docker.internal:8081" + path;
        } else if (path.startsWith("/workflows")) {
            return "http://host.docker.internal:8082" + path;
        } else {
            throw new IllegalArgumentException("Unknown service");
        }
    }
}
