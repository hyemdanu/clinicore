package com.clinicore.project.entity;

//imports
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//annotations
@Entity
@Table(name = "caregiver")
@Getter
@Setter
@NoArgsConstructor


public class CareGiverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column
    private String notes;

}