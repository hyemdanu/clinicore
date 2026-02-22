package com.clinicore.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// composite key for the resident_caregiver table
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ResidentCaregiverId implements Serializable {

    @Column(name = "resident_id")
    private Long residentId;

    @Column(name = "caregiver_id")
    private Long caregiverId;
}
