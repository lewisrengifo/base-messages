package com.lewisrp.basemessages.backend.adapter.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "storage.s3")
public class StorageProperties {
    private String endpoint = "http://localhost:9000";
    private String region = "us-east-1";
    private String bucket = "base-messages";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private boolean pathStyleAccess = true;
    private Duration presignedUrlExpiry = Duration.ofHours(1);
}
