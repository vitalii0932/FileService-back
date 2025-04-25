package org.example.fileservice.services;

import org.example.fileservice.models.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileService {
    List<Map<String, String>> getAllFiles();
    Map<String, String> uploadFile(MultipartFile file) throws IOException;
    File loadFile(Long id);
    boolean renameFile(Long id, String newName);
    boolean starFile(Long id);
    boolean moveToTrash(Long id);
    boolean deleteFile(Long id);
}
