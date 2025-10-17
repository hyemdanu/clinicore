package com.clinicore.project.entity;

// imports
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

// annotations
@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor

public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
