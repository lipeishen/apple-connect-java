package com.apple.sdk;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by lipeishen on 2020/02/11.
 */
@Data
@Accessors(fluent = true)
public abstract class AbstractAppleApiClient {

    private Integer tokenRefreshInterval = 15;
    private Integer tokenExpiration = 20;
    private String algorithm = "ES256";
    private String tokenType = "JWT";
    private String audience = "appstoreconnect-v1";
    private String baseUrl = "https://api.appstoreconnect.apple.com";
    private String privateSecPath;
    private String privateSecAlgorithm = "EC";
    private String issuer;
    private String keyId;
    private String key;
    private Long subjectId;

    public AbstractAppleApiClient() {
    }

    public AbstractAppleApiClient(String key, String issuer, String keyId) {
        this.key = key;
        this.issuer = issuer;
        this.keyId = keyId;
    }
}
