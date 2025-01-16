package com.edms.workflows.helper;

import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class Microservice {

    private final RestTemplate restTemplate;
    public static final String GENERAL_ROUTE = "/api/v1";

    // Hardcoded values for the URLs and secret
    private String fileManagementUrl = "http://file-management:8081/file-management";
    private String workflowsUrl = "http://workflows:8082/workflows";
    private String tasksUrl = "http://tasks:8083/tasks";
    private String formsUrl = "http://forms:8084/forms";
    private String secret = "my-proxy-secret-key";

    public Microservice() {
        this.restTemplate = new RestTemplate();
    }

    // Generic GET request
    public <T> ResponseEntity<T> get(String url, Class<T> responseType) {
        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            responseType
        );
    }

    // Generic GET request with type reference for collections
    public <T> ResponseEntity<T> get(String url, ParameterizedTypeReference<T> responseType) {
        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            responseType
        );
    }

    // Generic POST request
    public <T> ResponseEntity<T> post(String url, Object body, Class<T> responseType) {
        return restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(body, createHeaders()),
            responseType
        );
    }

    // Generic PUT request
    public <T> ResponseEntity<T> put(String url, Object body, Class<T> responseType) {
        return restTemplate.exchange(
            url,
            HttpMethod.PUT,
            new HttpEntity<>(body, createHeaders()),
            responseType
        );
    }

    // Generic DELETE request
    public <T> ResponseEntity<T> delete(String url, Class<T> responseType) {
        return restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            new HttpEntity<>(createHeaders()),
            responseType
        );
    }

    // Helper method to create headers
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Proxy-Secret", secret);
        return headers;
    }

    public String getFileManagementRoute(String endpoint) {
        return fileManagementUrl + GENERAL_ROUTE + endpoint;
    }

    public String getWorkflowsRoute(String endpoint) {
        return workflowsUrl + GENERAL_ROUTE + endpoint;
    }

    public String getTasksRoute(String endpoint) {
        return tasksUrl + GENERAL_ROUTE + endpoint;
    }

    public String getFormsRoute(String endpoint) {
        return formsUrl + GENERAL_ROUTE + endpoint;
    }
}
