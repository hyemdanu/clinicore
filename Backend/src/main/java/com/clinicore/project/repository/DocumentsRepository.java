package com.clinicore.project.repository;
import java.time.LocalDateTime;
import java.util.List;
import com.clinicore.project.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DocumentsRepository extends JpaRepository<Document, Long> {

    /**
     * Projection interface for document metadata (excludes binary content)
     */
    interface DocumentMetadata {
        Long getId();
        String getTitle();
        String getType();
        Long getResidentId();
        LocalDateTime getUploaded_at();
    }

    /**
     * Get all document metadata without loading binary content
     */
    @Query("SELECT d.id as id, d.title as title, d.type as type, d.residentId as residentId, d.uploaded_at as uploaded_at FROM Document d")
    List<DocumentMetadata> findAllMetadata();

    /**
     * Get document metadata for a specific resident without loading binary content
     */
    @Query("SELECT d.id as id, d.title as title, d.type as type, d.residentId as residentId, d.uploaded_at as uploaded_at FROM Document d WHERE d.residentId = :residentId")
    List<DocumentMetadata> findMetadataByResidentId(@Param("residentId") Long residentId);

    /**
     * Get all documents for a specific resident (includes binary content)
     */
    List<Document> findByResidentId(Long residentId);

    /**
     * Delete all documents for a specific resident (e.g., if they are discharged)
     */
    @Transactional
    void deleteByResidentId(Long residentId);
}