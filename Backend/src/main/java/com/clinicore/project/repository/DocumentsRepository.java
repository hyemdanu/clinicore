package com.clinicore.project.repository;
import java.util.List;
import com.clinicore.project.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentsRepository extends JpaRepository<Document, Long> {

    /**
     * Get all documents for a specific resident
     */
    List<Document> findByResidentId(Long residentId);

    /**
     * Find a document by its title (useful for checking duplicates)
     */
    Document findByTitle(String title);

    /**
     * Delete all documents for a specific resident (e.g., if they are discharged)
     */
    void deleteByResidentId(Long residentId);

    /**
     * Get all documents of a specific type (e.g., "Medical Report", "Consent Form")
     */
    List<Document> findByType(String type);
}