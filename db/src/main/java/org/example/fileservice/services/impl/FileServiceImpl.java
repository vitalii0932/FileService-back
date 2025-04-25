package org.example.fileservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fileservice.models.File;
import org.example.fileservice.repositories.FileRepository;
import org.example.fileservice.repositories.UserRepository;
import org.example.fileservice.services.FileService;
import org.example.fileservice.services.LZ77;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final LZ77 lz77;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, String>> getAllFiles() {
        var user = userRepository.findById(1L).orElseThrow();
        var files = fileRepository.findAllByUserId(user.getId());

        return files.stream()
                .map(file -> Map.of(
                        "id", file.getId().toString(),
                        "name", file.getFileName(),
                        "size", refactorFileSize(file.getFileSize()),
                        "lastChange", file.getChangeDate().toLocalDate().toString(),
                        "type", file.getFileType(),
                        "isInTrash", file.getIsInTrash().toString(),
                        "isStarred", file.getIsStarred().toString())
                )
                .toList();
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file) throws IOException {
        var compressed = lz77.compress(file.getBytes());
        var user = userRepository.findById(1L).orElseThrow();

        var fileName = Objects.requireNonNull(file.getOriginalFilename()).substring(0, file.getOriginalFilename().lastIndexOf('.'));
        var fileType = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf('.') + 1);

        var fileEntity = File.builder()
                .fileName(fileName)
                .filePath("path")
                .fileSize((long) compressed.length)
                .changeDate(LocalDateTime.now())
                .contentType(file.getContentType())
                .fileType(fileType)
                .fileData(compressed)
                .user(user)
                .isInTrash(false)
                .isStarred(false)
                .build();

        fileEntity = fileRepository.save(fileEntity);

        return Map.of("id", fileEntity.getId().toString(),
                "name", fileEntity.getFileName(),
                "size", refactorFileSize(fileEntity.getFileSize()),
                "lastChange", fileEntity.getChangeDate().toLocalDate().toString(),
                "type", fileEntity.getFileType(),
                "isInTrash", fileEntity.getIsInTrash().toString(),
                "isStarred", fileEntity.getIsStarred().toString());
    }

    @Override
    public File loadFile(Long id) {
        var file = fileRepository.findById(id).orElseThrow();

        file.setFileData(lz77.decompress(file.getFileData()));

        return file;
    }

    @Override
    public boolean renameFile(Long id, String newName) {
        var file = fileRepository.findById(id).orElseThrow();

        file.setFileName(newName);
        file.setChangeDate(LocalDateTime.now());

        fileRepository.save(file);
        return true;
    }

    @Override
    public boolean starFile(Long id) {
        var file = fileRepository.findById(id).orElseThrow();
        file.setIsStarred(!file.getIsStarred());
        fileRepository.save(file);

        return true;
    }

    @Override
    public boolean moveToTrash(Long id) {
        var file = fileRepository.findById(id).orElseThrow();
        file.setIsInTrash(!file.getIsInTrash());

        fileRepository.save(file);

        return true;
    }

    @Override
    @Transactional
    public boolean deleteFile(Long id) {
        fileRepository.deleteById(id);
        return true;
    }

    private String refactorFileSize(Long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return size / 1024 + " KB";
        } else {
            return size / (1024 * 1024) + " MB";
        }
    }
}
