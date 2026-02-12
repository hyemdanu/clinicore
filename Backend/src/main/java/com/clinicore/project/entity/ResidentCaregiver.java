package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// maps the resident_caregiver
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resident_caregiver")
public class ResidentCaregiver {

    @EmbeddedId
    private ResidentCaregiverId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("residentId")
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("caregiverId")
    @JoinColumn(name = "caregiver_id")
    private Caregiver caregiver;

    // when the caregiver got assigned to this resident
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
