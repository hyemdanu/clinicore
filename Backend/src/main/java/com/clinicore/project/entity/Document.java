package com.clinicore.project.entity;

// imports
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//annotations
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "documents")

public class DocumentEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column(name = "resident_id", nullable = false)
    private Long resident_Id;

    @Column
    private byte[] document;

    @Column
    private String type;

}


