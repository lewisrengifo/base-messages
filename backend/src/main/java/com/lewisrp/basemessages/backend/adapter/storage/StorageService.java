package com.lewisrp.basemessages.backend.adapter.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final StorageProperties properties;

    private S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));

        if (properties.isPathStyleAccess()) {
            builder.forcePathStyle(true);
        }

        builder.endpointOverride(URI.create(properties.getEndpoint()));
        return builder.build();
    }

    private S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));

        if (properties.isPathStyleAccess()) {
            builder.serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }

        builder.endpointOverride(URI.create(properties.getEndpoint()));
        return builder.build();
    }

    /**
     * Upload a file to S3/MinIO and return the object key.
     */
    public String uploadFile(String folder, String originalFilename, byte[] content, String contentType) {
        String key = folder + "/" + UUID.randomUUID() + "-" + originalFilename;

        try (S3Client s3 = s3Client()) {
            ensureBucketExists(s3);

            s3.putObject(PutObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));

            log.info("Uploaded file to S3: bucket={}, key={}", properties.getBucket(), key);
            return key;
        }
    }

    /**
     * Generate a presigned URL for temporary access to a file.
     */
    public String generatePresignedUrl(String key, Duration expiry) {
        try (S3Presigner presigner = s3Presigner()) {
            var presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry != null ? expiry : properties.getPresignedUrlExpiry())
                    .getObjectRequest(b -> b.bucket(properties.getBucket()).key(key))
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    /**
     * Generate a presigned URL using default expiry.
     */
    public String generatePresignedUrl(String key) {
        return generatePresignedUrl(key, null);
    }

    /**
     * Download a file from S3.
     */
    public byte[] downloadFile(String key) {
        try (S3Client s3 = s3Client()) {
            return s3.getObject(GetObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(key)
                            .build())
                    .readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download file from S3: key={}", key, e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    /**
     * Delete a file from S3.
     */
    public void deleteFile(String key) {
        try (S3Client s3 = s3Client()) {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build());
            log.info("Deleted file from S3: key={}", key);
        }
    }

    private void ensureBucketExists(S3Client s3) {
        try {
            s3.headBucket(HeadBucketRequest.builder()
                    .bucket(properties.getBucket())
                    .build());
        } catch (NoSuchBucketException e) {
            s3.createBucket(CreateBucketRequest.builder()
                    .bucket(properties.getBucket())
                    .build());
            // Set public read policy for the bucket
            String policy = String.format(
                    "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"s3:GetObject\",\"Resource\":\"arn:aws:s3:::%s/*\"}]}",
                    properties.getBucket());
            s3.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(properties.getBucket())
                    .policy(policy)
                    .build());
            log.info("Created S3 bucket: {}", properties.getBucket());
        }
    }
}
