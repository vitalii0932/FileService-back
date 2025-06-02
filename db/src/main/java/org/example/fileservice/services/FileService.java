package org.example.fileservice.services;

import org.example.fileservice.models.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileService {
    List<Map<String, String>> getAllFiles(String sub);
    Map<String, String> uploadFile(String sub, MultipartFile file) throws IOException;
    File loadFile(String sub, Long id);
    boolean renameFile(String sub, Long id, String newName);
    boolean starFile(String sub, Long id);
    boolean moveToTrash(String sub, Long id);
    boolean deleteFile(String sub, Long id);
}
