package com.operator.infrastructure.publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.operator.core.publish.domain.PublishHistory;
import com.operator.core.publish.domain.PublishStatus;
import com.operator.infrastructure.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * REST Publisher Service
 *
 * Publishes operators/packages to REST endpoints
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RestPublisherService {

    private static final Logger log = LoggerFactory.getLogger(RestPublisherService.class);

    private final RestTemplate restTemplate;

    /**
     * Publish to REST endpoint
     */
    public PublishResult publish(PublishContext context) {
        log.info("Publishing to REST endpoint: {}", context.getEndpoint());

        try {
            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (context.getAuthToken() != null) {
                headers.setBearerAuth(context.getAuthToken());
            }

            // Add custom headers from configuration
            if (context.getCustomHeaders() != null) {
                context.getCustomHeaders().forEach(headers::add);
            }

            HttpEntity<String> entity = new HttpEntity<>(context.getPayload(), headers);

            // Execute request
            var response = restTemplate.exchange(
                    context.getEndpoint(),
                    HttpMethod.valueOf(context.getHttpMethod()),
                    entity,
                    String.class
            );

            log.info("REST publish successful: {} - {}", context.getEndpoint(), response.getStatusCode());

            return new PublishResult(
                    true,
                    "Published successfully",
                    context.getEndpoint(),
                    response.getStatusCode().value(),
                    response.getBody()
            );

        } catch (Exception e) {
            log.error("REST publish failed", e);

            return new PublishResult(
                    false,
                    e.getMessage(),
                    context.getEndpoint(),
                    null,
                    null
            );
        }
    }

    /**
     * Test REST endpoint connectivity
     */
    public boolean testConnectivity(String endpoint, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (authToken != null) {
                headers.setBearerAuth(authToken);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Connectivity test failed for endpoint: {}", endpoint, e);
            return false;
        }
    }

    /**
     * Publish result
     */
    public record PublishResult(
            boolean success,
            String message,
            String endpoint,
            Integer httpStatus,
            String responseBody
    ) {
    }

    /**
     * Publish context
     */
    public static class PublishContext {
        private String endpoint;
        private String httpMethod = "POST";
        private String payload;
        private String authToken;
        private java.util.Map<String, String> customHeaders;
        private java.util.Map<String, Object> metadata;

        public String getEndpoint() { return endpoint; }
        public String getHttpMethod() { return httpMethod; }
        public String getPayload() { return payload; }
        public String getAuthToken() { return authToken; }
        public java.util.Map<String, String> getCustomHeaders() { return customHeaders; }
        public java.util.Map<String, Object> getMetadata() { return metadata; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final PublishContext context = new PublishContext();

            public Builder endpoint(String endpoint) {
                context.endpoint = endpoint;
                return this;
            }

            public Builder httpMethod(String method) {
                context.httpMethod = method;
                return this;
            }

            public Builder payload(String payload) {
                context.payload = payload;
                return this;
            }

            public Builder authToken(String token) {
                context.authToken = token;
                return this;
            }

            public Builder headers(java.util.Map<String, String> headers) {
                context.customHeaders = headers;
                return this;
            }

            public Builder metadata(java.util.Map<String, Object> metadata) {
                context.metadata = metadata;
                return this;
            }

            public PublishContext build() {
                return context;
            }
        }
    }
}
