package org.example.fileservice.controlles;

import jakarta.annotation.PostConstruct;
import org.example.fileservice.dto.UpdateProfileRequest;
import org.example.fileservice.models.User;
import org.example.fileservice.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class ProfileController {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.management-api-token}")
    private String managementApiToken;

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    private final UserService userService;

    public ProfileController(WebClient.Builder webClientBuilder, UserService userService) {
        this.webClientBuilder = webClientBuilder;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        System.out.println("auth0Domain: " + auth0Domain);
        if (auth0Domain == null) {
            throw new IllegalStateException("auth0.domain is not configured in application.properties");
        }
        this.webClient = webClientBuilder.baseUrl("https://" + auth0Domain).build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        return userService.findBySub(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/update-profile")
    public Mono<ResponseEntity<UpdateProfileResponse>> updateProfile(@RequestBody UpdateProfileRequest request) {
        if (request.getPicture() == null || request.getPicture().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(new UpdateProfileResponse(false, "Picture cannot be empty")));
        }
        if (request.getPicture().length() > 1000000) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(new UpdateProfileResponse(false, "Picture size exceeds 1MB limit")));
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(new UpdateProfileResponse(false, "Custom name cannot be empty")));
        }

        UpdateProfilePayload payload = new UpdateProfilePayload(
                new UserMetadata(request.getPicture(), request.getName())
        );
        return webClient
                .patch()
                .uri("/api/v2/users/{userId}", request.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + managementApiToken)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Object.class)
                .flatMap(response -> {
                    User updatedUser = userService.updateUser(
                            request.getUserId(),
                            request.getName(),
                            request.getPicture()
                    );
                    return Mono.just(ResponseEntity.ok().body(new UpdateProfileResponse(true, updatedUser)));
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error updating profile: " + e.getMessage());
                    System.err.println("Response body: " + e.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.status(e.getStatusCode())
                            .body(new UpdateProfileResponse(false, e.getResponseBodyAsString())));
                })
                .onErrorResume(e -> {
                    System.err.println("Unexpected error: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new UpdateProfileResponse(false, e.getMessage())));
                });
    }

    private static class UpdateProfilePayload {
        private UserMetadata user_metadata;

        public UpdateProfilePayload(UserMetadata user_metadata) {
            this.user_metadata = user_metadata;
        }

        public UserMetadata getUser_metadata() { return user_metadata; }
    }

    private static class UserMetadata {
        private String picture;
        private String custom_name;

        public UserMetadata(String picture, String customName) {
            this.picture = picture;
            this.custom_name = customName;
        }

        public String getPicture() { return picture; }
        public String getCustom_name() { return custom_name; }

        @Override
        public String toString() {
            return "UserMetadata{picture='" + picture + "', custom_name='" + custom_name + "'}";
        }
    }

    private static class UpdateProfileResponse {
        private boolean success;
        private Object data;

        public UpdateProfileResponse(boolean success, Object data) {
            this.success = success;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public Object getData() { return data; }
    }
}