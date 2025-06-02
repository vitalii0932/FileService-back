package org.example.fileservice.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String sub;
    private String email;
    private String name;
    private String picture;
}
