package org.example.fileservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.client-id}")
    private String clientId;

    @Value("${auth0.client-secret}")
    private String clientSecret;

    @Bean
    public WebClient auth0WebClient() {
        return WebClient.builder()
                .baseUrl("https://" + auth0Domain)
                .build();
    }

    @Bean
    public String managementApiToken(WebClient auth0WebClient) throws Exception {
        return auth0WebClient
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new Auth0TokenRequest(clientId, clientSecret, "https://" + auth0Domain + "/api/v2/", "client_credentials"))
                .retrieve()
                .bodyToMono(Auth0TokenResponse.class)
                .map(Auth0TokenResponse::getAccessToken)
                .block();
    }

    private static class Auth0TokenRequest {
        private String client_id;
        private String client_secret;
        private String audience;
        private String grant_type;

        public Auth0TokenRequest(String clientId, String clientSecret, String audience, String grantType) {
            this.client_id = clientId;
            this.client_secret = clientSecret;
            this.audience = audience;
            this.grant_type = grantType;
        }

        public String getClient_id() {
            return client_id;
        }

        public String getClient_secret() {
            return client_secret;
        }

        public String getAudience() {
            return audience;
        }

        public String getGrant_type() {
            return grant_type;
        }
    }

    private static class Auth0TokenResponse {
        private String access_token;

        public String getAccessToken() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }
    }
}