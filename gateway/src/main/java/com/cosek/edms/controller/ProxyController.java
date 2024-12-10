package com.cosek.edms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.Enumeration;

@RestController
@CrossOrigin(origins = "*")
public class ProxyController {

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
            // Handle multipart requests (file uploads)
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

                // Extract HTTP method
                HttpMethod method = HttpMethod.valueOf(request.getMethod());

                // Construct backend URL
                String path = request.getRequestURI();
                String backendUrl = getBackendUrl(path);

                String queryString = request.getQueryString();
                if (queryString != null) {
                    backendUrl += "?" + queryString;
                }

                // Prepare headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    headers.add(headerName, request.getHeader(headerName));
                }
                headers.set("X-Proxy-Secret", proxySecret);

                // Create MultiValueMap for request parts
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

                // Get fileData and file
                String fileDataJson = multipartRequest.getParameter("fileData");
                MultipartFile file = multipartRequest.getFile("file");

                if (fileDataJson != null && file != null) {
                    // Convert file to ByteArrayResource to ensure full data transmission
                    ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };

                    // Add fileData as JSON string
                    body.add("fileData", fileDataJson);
                    
                    // Add file part using ByteArrayResource
                    body.add("file", fileResource);
                } else {
                    throw new IllegalArgumentException("Missing fileData or file in the request");
                }

                // Prepare HttpEntity with the MultiValueMap
                HttpEntity<MultiValueMap<String, Object>> forwardedRequest = new HttpEntity<>(body, headers);

                // Forward the multipart request to the backend
                ResponseEntity<String> response = restTemplate.exchange(backendUrl, method, forwardedRequest, String.class);

                // Log the response for debugging
                System.out.println("Backend Response Status: " + response.getStatusCode());
                System.out.println("Backend Response Body: " + response.getBody());

                return response;

            } else {
                // Existing non-multipart request handling remains the same
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

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Proxy error: File processing failed - " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Proxy error: " + e.getMessage());
        }
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