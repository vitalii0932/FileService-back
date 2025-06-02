package org.example.fileservice.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String userId;
    private String name; // Будет использоваться как custom_name
    private String picture;
}
