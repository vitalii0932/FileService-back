package org.example.fileservice.controlles;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.fileservice.dto.RenameRequest;
import org.example.fileservice.models.File;
import org.example.fileservice.services.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class FilesController {

    private final FileService fileService;

    @GetMapping("/all")
    public List<Map<String, String>> allFiles(@AuthenticationPrincipal Jwt jwt) {
        String sub = jwt.getSubject();
        return fileService.getAllFiles(sub);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam("file") MultipartFile file) {
        try {
            String sub = jwt.getSubject();
            return ResponseEntity.ok(fileService.uploadFile(sub, file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/star_file/{id}")
    public ResponseEntity<?> starFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") @NotNull Long id
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            String sub = jwt.getSubject();

            boolean moved = fileService.starFile(sub, id);
            if (moved) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid file id");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error");
        }
    }

    @GetMapping("/move_to_trash/{id}")
    public ResponseEntity<?> moveToTrash(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") @NotNull Long id
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            String sub = jwt.getSubject();

            boolean moved = fileService.moveToTrash(sub, id);
            if (moved) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid file id");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") @NotNull Long id
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            String sub = jwt.getSubject();

            boolean deleted = fileService.deleteFile(sub, id);
            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid file id");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error");
        }
    }

    @PatchMapping("/rename/{id}")
    public ResponseEntity<?> renameFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") @NotNull Long id,
            @RequestBody RenameRequest request
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            String sub = jwt.getSubject();

            boolean renamed = fileService.renameFile(sub, id, request.getNewName());
            if (renamed) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid file id");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error");
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFileById(@AuthenticationPrincipal Jwt jwt,
                                                     @PathVariable Long id) {
        try {
            String sub = jwt.getSubject();
            File file = fileService.loadFile(sub, id);

            if (file == null || file.getFileData() == null) {
                return ResponseEntity.notFound().build();
            }

            System.out.println(file.getContentType());
            System.out.println(file.getFileData().length);
            System.out.println(file.getFileSize());
            System.out.println(file.getFileName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            headers.setContentLength(file.getFileData().length);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8) + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getFileData());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
