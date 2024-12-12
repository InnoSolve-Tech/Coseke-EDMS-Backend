package com.cosek.edms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*")
public class ProxyController {

    private static final Logger logger = Logger.getLogger(ProxyController.class.getName());

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${proxy.secret}")
    private String proxySecret;

    public ProxyController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) {
        try {
            if (request instanceof MultipartHttpServletRequest) {
                return handleMultipartRequest((MultipartHttpServletRequest) request);
            } else {
                return handleStandardRequest(request);
            }
        } catch (IOException e) {
            logger.severe("Proxy error: File processing failed - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Proxy error: File processing failed - " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Proxy error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Proxy error: " + e.getMessage());
        }
    }

    private ResponseEntity<String> handleMultipartRequest(MultipartHttpServletRequest request) throws IOException {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String path = request.getRequestURI();
        String backendUrl = getBackendUrl(path);

        // Append query parameters to the backend URL if any
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

        // Prepare multipart data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        request.getMultiFileMap().forEach((key, files) -> {
            for (MultipartFile file : files) {
                try {
                    body.add(key, new InputStreamResource(file.getInputStream()) {
                        @Override
                        public long contentLength() {
                            return file.getSize();
                        }

                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    });
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                body.add(key, value);
            }
        });

        // Forward the multipart request
        HttpEntity<MultiValueMap<String, Object>> forwardedRequest = new HttpEntity<>(body, headers);
        try {
            return restTemplate.exchange(backendUrl, method, forwardedRequest, String.class);
        } catch (Exception e) {
            logger.severe("Error while forwarding multipart request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while forwarding multipart request: " + e.getMessage());
        }
    }

    private ResponseEntity<String> handleStandardRequest(HttpServletRequest request) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
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
        HttpEntity<String> forwardedRequest = new HttpEntity<>(null, headers);

        // Execute proxied request
        return restTemplate.exchange(backendUrl, method, forwardedRequest, String.class);
    }

    private String getBackendUrl(String path) {
        if (path.startsWith("/file-management")) {
            return "http://file-management:8081" + path;
        } else if (path.startsWith("/workflows")) {
            return "http://workflows:8082" + path;
        } else if (path.startsWith("/tasks")) {
            return "http://tasks:8083" + path;
        } else if (path.startsWith("/forms")) {
            return "http://forms:8084" + path;
        } else {
            throw new IllegalArgumentException("Unknown service path: " + path);
        }
    }
}
