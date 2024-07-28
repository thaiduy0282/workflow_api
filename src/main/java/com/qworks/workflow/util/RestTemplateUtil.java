package com.qworks.workflow.util;

import com.qworks.workflow.exception.SystemErrorException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestTemplateUtil {

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnRlZ3JhdGlvbnVzZXJAcXdvcmtzLmFpIiwiaWF0IjoxNzA5ODU4MTg2LCJleHAiOjE3NDEzOTQxODZ9.bX6zI0gH4LjSZyhCno0nkbvc-rfK6S0nHzuHaSGe3RRMXLYU-lCYdsV8wxutdcN-_DIH9j0aExE8h_N5PqgfAQ";
        headers.set("Authorization", "Bearer " + accessToken);

        return headers;
    }

    public static SimpleClientHttpRequestFactory getRequestFactory() {
        // Create a custom request factory to customize connection behavior
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection) {
                    // Ignore SSL certificate validation
                    ((HttpsURLConnection) connection).setSSLSocketFactory(createTrustfulSSLSocketFactory());
                    ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
        return requestFactory;
    }

    private static javax.net.ssl.SSLSocketFactory createTrustfulSSLSocketFactory() {
        try {
            // Create a trustful SSL socket factory to ignore certificate validation
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception e) {
            throw new SystemErrorException("Failed to create trustful SSL socket factory");
        }
    }
}
