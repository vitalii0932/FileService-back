package org.example.fileservice.controlles;

import org.example.fileservice.dto.UserDTO;
import org.example.fileservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<String> saveUser(@RequestBody UserDTO userDTO) {
        try {
            userService.createOrUpdateUser(
                    userDTO.getSub(),
                    userDTO.getEmail(),
                    userDTO.getName(),
                    userDTO.getPicture()
            );
            return ResponseEntity.ok("User saved successfully");
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body("Error saving user: " + e.getMessage());
        }
    }
}
