package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents", indexes = {
    @Index(name = "idx_document_resident_id", columnList = "resident_id"),
    @Index(name = "idx_document_type", columnList = "type")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] document;

    private String type;

    @Column(updatable = false)
    private LocalDateTime uploaded_at;

    @PrePersist
    protected void onCreate() {
        uploaded_at = LocalDateTime.now();
    }
}