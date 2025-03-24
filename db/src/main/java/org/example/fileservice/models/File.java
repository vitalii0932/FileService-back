package org.example.fileservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "files")
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_path")
    private String filePath;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(name = "change_date")
    private LocalDateTime changeDate;
    @Column(name = "content_type")
    private String contentType;
    @Lob
    @Column(name = "file_data")
    private byte[] fileData;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
