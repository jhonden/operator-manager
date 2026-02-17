package com.operator.infrastructure.publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * File Publisher Service
 *
 * Publishes operators/packages to local file system
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FilePublisherService {

    private static final Logger log = LoggerFactory.getLogger(FilePublisherService.class);

    private final RestTemplate restTemplate;

    /**
     * Publish to local file system
     */
    public PublishResult publish(PublishContext context) {
        log.info("Publishing to file: {}", context.getTargetPath());

        try {
            Path targetPath = Path.of(context.getTargetPath());

            // Create parent directories
            Files.createDirectories(targetPath.getParent());

            // Write content
            if (context.getContent() != null) {
                Files.writeString(targetPath, context.getContent(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else if (context.getContentBytes() != null) {
                Files.write(targetPath, context.getContentBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            log.info("File published successfully: {}", targetPath);

            return new PublishResult(
                    true,
                    "Published successfully",
                    targetPath.toString()
            );

        } catch (Exception e) {
            log.error("File publish failed", e);

            return new PublishResult(
                    false,
                    e.getMessage(),
                    context.getTargetPath()
            );
        }
    }

    /**
     * Delete published file
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Path.of(filePath);
            return Files.deleteIfExists(path);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Check if published file exists
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Path.of(filePath));
    }

    /**
     * Publish result
     */
    public record PublishResult(
            boolean success,
            String message,
            String targetPath
    ) {
    }

    /**
     * Publish context
     */
    public static class PublishContext {
        private String targetPath;
        private String content;
        private byte[] contentBytes;
        private String contentType;
        private java.util.Map<String, Object> metadata;

        public String getTargetPath() { return targetPath; }
        public String getContent() { return content; }
        public byte[] getContentBytes() { return contentBytes; }
        public String getContentType() { return contentType; }
        public java.util.Map<String, Object> getMetadata() { return metadata; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final PublishContext context = new PublishContext();

            public Builder targetPath(String path) {
                context.targetPath = path;
                return this;
            }

            public Builder content(String content) {
                context.content = content;
                return this;
            }

            public Builder contentBytes(byte[] bytes) {
                context.contentBytes = bytes;
                return this;
            }

            public Builder contentType(String contentType) {
                context.contentType = contentType;
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
