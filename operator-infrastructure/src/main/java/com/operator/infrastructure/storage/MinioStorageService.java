package com.operator.infrastructure.storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO Storage Service
 *
 * Handles file storage operations using MinIO (S3-compatible object storage)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Service
@ConditionalOnProperty(name = "operator.storage.type", havingValue = "minio")
public class MinioStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${minio.write-timeout:60000}")
    private int writeTimeout;

    @Value("${minio.read-timeout:10000}")
    private int readTimeout;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Check if bucket exists, create if not
            if (!bucketExists(bucketName)) {
                makeBucket(bucketName);
                log.info("Created MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client", e);
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }

    /**
     * Upload file to MinIO
     */
    public String uploadFile(String objectName, InputStream inputStream,
                             long size, String contentType) throws Exception {
        log.info("Uploading file to MinIO: {} (size: {})", objectName, size);

        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build();

        minioClient.putObject(putObjectArgs);

        log.info("File uploaded successfully: {}", objectName);
        return getObjectUrl(objectName);
    }

    /**
     * Upload file from MultipartFile
     */
    public String uploadFile(String objectName, MultipartFile file) throws Exception {
        log.info("Uploading MultipartFile to MinIO: {}", objectName);

        try (InputStream inputStream = file.getInputStream()) {
            return uploadFile(objectName, inputStream, file.getSize(), file.getContentType());
        }
    }

    /**
     * Download file from MinIO
     */
    public InputStream downloadFile(String objectName) throws Exception {
        log.debug("Downloading file from MinIO: {}", objectName);

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();

        return minioClient.getObject(getObjectArgs);
    }

    /**
     * Delete file from MinIO
     */
    public void deleteFile(String objectName) throws Exception {
        log.info("Deleting file from MinIO: {}", objectName);

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();

        minioClient.removeObject(removeObjectArgs);
        log.info("File deleted successfully: {}", objectName);
    }

    /**
     * Delete multiple files
     */
    public void deleteFiles(List<String> objectNames) throws Exception {
        log.info("Deleting {} files from MinIO", objectNames.size());

        for (String objectName : objectNames) {
            deleteFile(objectName);
        }

        log.info("Files deleted successfully: {} files", objectNames.size());
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String objectName) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.statObject(statObjectArgs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get file metadata
     */
    public StatObjectResponse getFileInfo(String objectName) throws Exception {
        log.debug("Getting file info: {}", objectName);

        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();

        return minioClient.statObject(statObjectArgs);
    }

    /**
     * Get file URL (presigned URL for temporary access)
     */
    public String getPresignedUrl(String objectName, int expirySeconds) throws Exception {
        log.debug("Generating presigned URL for: {} (expires: {}s)", objectName, expirySeconds);

        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(io.minio.http.Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expirySeconds, TimeUnit.SECONDS)
                .build();

        return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
    }

    /**
     * Copy file within MinIO
     */
    public void copyFile(String sourceObject, String destinationObject) throws Exception {
        log.info("Copying file: {} -> {}", sourceObject, destinationObject);

        CopyObjectArgs copyObjectArgs = CopyObjectArgs.builder()
                .bucket(bucketName)
                .object(destinationObject)
                .source(CopySource.builder()
                        .bucket(bucketName)
                        .object(sourceObject)
                        .build())
                .build();

        minioClient.copyObject(copyObjectArgs);
        log.info("File copied successfully");
    }

    /**
     * List files in bucket with prefix
     */
    public List<Result> listFiles(String prefix) throws Exception {
        log.debug("Listing files with prefix: {}", prefix);

        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build();

        Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);

        return streamToList(results);
    }

    /**
     * Check if bucket exists
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to check bucket existence", e);
            return false;
        }
    }

    /**
     * Create bucket
     */
    public void makeBucket(String bucketName) throws Exception {
        minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(bucketName)
                .build());
    }

    /**
     * Delete bucket
     */
    public void deleteBucket(String bucketName) throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket(bucketName)
                .build());
    }

    /**
     * List all buckets
     */
    public List<Bucket> listBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    /**
     * Get object URL (public access URL format)
     */
    public String getObjectUrl(String objectName) {
        return String.format("%s/%s/%s", endpoint, bucketName, objectName);
    }

    /**
     * Upload operator code file
     */
    public String uploadOperatorCode(Long operatorId, Long versionId,
                                     String fileName, MultipartFile file) throws Exception {
        String objectName = buildOperatorPath(operatorId, versionId, fileName);
        return uploadFile(objectName, file);
    }

    /**
     * Upload package file
     */
    public String uploadPackageFile(Long packageId, Long versionId,
                                    String fileName, MultipartFile file) throws Exception {
        String objectName = buildPackagePath(packageId, versionId, fileName);
        return uploadFile(objectName, file);
    }

    /**
     * Upload task artifact
     */
    public String uploadTaskArtifact(Long taskId, String artifactName,
                                     MultipartFile file) throws Exception {
        String objectName = buildTaskPath(taskId, artifactName);
        return uploadFile(objectName, file);
    }

    /**
     * Build operator storage path
     */
    private String buildOperatorPath(Long operatorId, Long versionId, String fileName) {
        return String.format("operators/%d/versions/%d/%s", operatorId, versionId, fileName);
    }

    /**
     * Build package storage path
     */
    private String buildPackagePath(Long packageId, Long versionId, String fileName) {
        return String.format("packages/%d/versions/%d/%s", packageId, versionId, fileName);
    }

    /**
     * Build task storage path
     */
    private String buildTaskPath(Long taskId, String fileName) {
        return String.format("tasks/%d/%s", taskId, fileName);
    }

    /**
     * Convert iterable to list
     */
    private List<Result> streamToList(Iterable<Result<Item>> iterable) {
        List<Result> list = new java.util.ArrayList<>();
        for (Result item : iterable) {
            list.add(item);
        }
        return list;
    }
}
