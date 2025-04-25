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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class FilesController {

    private final FileService fileService;

    @GetMapping("/all")
    public List<Map<String, String>> allFiles() {
        return fileService.getAllFiles();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(fileService.uploadFile(file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("id") Long id) {
        try {
            File file = fileService.loadFile(id);

            if (file == null || file.getFileData() == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            headers.setContentLength(file.getFileSize());
            headers.setContentDispositionFormData("attachment", file.getFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getFileData());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/star_file/{id}")
    public ResponseEntity<?> starFile(
            @PathVariable("id") @NotNull Long id
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            boolean moved = fileService.starFile(id);
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
            @PathVariable("id") @NotNull Long id
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            boolean moved = fileService.moveToTrash(id);
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
            @PathVariable("id") @NotNull Long id
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            boolean deleted = fileService.deleteFile(id);
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
            @PathVariable("id") @NotNull Long id,
            @RequestBody RenameRequest request
    ) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid file id");
            }

            boolean renamed = fileService.renameFile(id, request.getNewName());
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
    public ResponseEntity<Resource> downloadFileById(@PathVariable Long id) {
        try {
            File file = fileService.loadFile(id);

            ByteArrayResource resource = new ByteArrayResource(file.getFileData());
            long contentLength = file.getFileData().length;

            // Encode filename to handle non-ASCII characters
            String filename = file.getFileName() + "." + file.getFileType();
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

            // Set Content-Disposition with encoded filename
            String contentDisposition = "attachment; filename*=UTF-8''" + encodedFilename;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .contentLength(contentLength)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
