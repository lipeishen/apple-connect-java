package com.apple.sdk.utils.http;

/**
 * httpclient相关配置
 *
 * @author lipeishen
 * @date 2020/02/20
 * @since 1.0
 */

public class HttpClientConfig {
    private static final int DEFAULT_SOCKET_TIME_OUT = 30 * 1000;
    private static final int DEFAULT_CONNECTION_TIME_OUT = 30 * 1000;
    private static final int DEFAULT_CONNECTION_REQUEST_TIME_OUT = 30 * 1000;
    private static final String DEFAULT_CHARSET = "UTF-8";

    private int socketTimeOut = DEFAULT_SOCKET_TIME_OUT;
    private int connectionTimeOut = DEFAULT_CONNECTION_TIME_OUT;
    private int connectionRequestTimeOut = DEFAULT_CONNECTION_REQUEST_TIME_OUT;
    private String charset = DEFAULT_CHARSET;

    private HttpClientConfig() {
    }

    public int getSocketTimeOut() {
        return socketTimeOut;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public int getConnectionRequestTimeOut() {
        return connectionRequestTimeOut;
    }

    public void setConnectionRequestTimeOut(int connectionRequestTimeOut) {
        this.connectionRequestTimeOut = connectionRequestTimeOut;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int socketTimeOut = -1;
        private int connectionTimeOut = -1;
        private int connectionRequestTimeOut = -1;
        private String charset = null;

        public HttpClientConfig build() {
            HttpClientConfig instance = new HttpClientConfig();
            if (this.socketTimeOut > 0) {
                instance.socketTimeOut = this.socketTimeOut;
            }
            if (this.connectionTimeOut > 0) {
                instance.connectionTimeOut = this.connectionTimeOut;
            }
            if (this.connectionRequestTimeOut > 0) {
                instance.connectionRequestTimeOut = this.connectionRequestTimeOut;
            }
            if (this.charset != null) {
                instance.charset = this.charset;
            }
            return instance;
        }

        public int getSocketTimeOut() {
            return socketTimeOut;
        }

        public void setSocketTimeOut(int socketTimeOut) {
            this.socketTimeOut = socketTimeOut;
        }

        public int getConnectionTimeOut() {
            return connectionTimeOut;
        }

        public void setConnectionTimeOut(int connectionTimeOut) {
            this.connectionTimeOut = connectionTimeOut;
        }

        public int getConnectionRequestTimeOut() {
            return connectionRequestTimeOut;
        }

        public void setConnectionRequestTimeOut(int connectionRequestTimeOut) {
            this.connectionRequestTimeOut = connectionRequestTimeOut;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }
    }
}
