package com.clinicore.project.entity;

// imports
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

//annotations
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "communication_portal")

public class CommunicationPortalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portal_id;

    @Column(name = "sender_id", nullable = false)
    private Long sender_id;

    @Column
    private String sender_role;

    @Column(name = "recipient_id ", nullable = false)
    private Long recipient_id;

    @Column
    private String recipient_role;

    @Column
    private String subject;

    @Column
    private String message;

    @Column
    private LocalDate sent_date;

    @Column
    private LocalTime sent_time;

}