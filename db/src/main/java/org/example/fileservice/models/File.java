package org.example.fileservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "files")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "fileData")
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
    @Column(name = "file_type")
    private String fileType;
    @Column(name = "content_type")
    private String contentType;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_data")
    private byte[] fileData;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "is_in_trash")
    private Boolean isInTrash;
    @Column(name = "is_starred")
    private Boolean isStarred;
}
