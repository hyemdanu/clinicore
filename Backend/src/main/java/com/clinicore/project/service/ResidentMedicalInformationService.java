package com.clinicore.project.service;

import com.clinicore.project.dto.CapabilityDTO;
import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import com.clinicore.project.util.HashUtil;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resident Medical Information Service
 * Handles all resident medical data like insurance, allergies, diagnoses, etc.
 *
 * SHA-256 hashing is used for data integrity verification:
 *   - computeMedicalProfileHash()  covers: insurance, notes
 *   - computeMedicalRecordHash()   covers: allergy, diagnosis, notes
 *   - computeMedicalServicesHash() covers: hospiceAgency, preferredHospital,
 *                                          preferredPharmacy, homeHealthAgency,
 *                                          mortuary, dnrPolst, hospice, homeHealth, notes
 *
 * Hashes are stored in a `data_hash` column on each table (see DB migration).
 * Call updateXxxHash() whenever you save or update one of these entities.
 * The verifyXxxIntegrity() helpers throw SecurityException if tampering is detected.
 */
@Service
public class ResidentMedicalInformationService {

    // repositories for db access
    private final MedicalProfileRepository medicalProfileRepository;
    private final UserProfileRepository userProfileRepository;

    public ResidentMedicalInformationService(MedicalProfileRepository medicalProfileRepository,
                                             UserProfileRepository userProfileRepository) {
        this.medicalProfileRepository = medicalProfileRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // -------------------------------------------------------------------------
    // Hash computation — one method per entity
    // These build the exact same string every time so hashes are reproducible.
    // Fields are joined with "|" so "a|b" and "ab" never collide.
    // -------------------------------------------------------------------------

    /**
     * Builds the hash input string for MedicalProfile (insurance + notes).
     */
    private String computeMedicalProfileHash(MedicalProfile p) {
        String data = nullSafe(p.getInsurance())
                + "|" + nullSafe(p.getNotes());
        return HashUtil.sha256(data);
    }

    /**
     * Builds the hash input string for MedicalRecord (allergy, diagnosis, notes).
     * Uses the raw comma-separated strings, NOT the parsed lists, so the hash
     * stays consistent between saves and reads.
     */
    private String computeMedicalRecordHash(MedicalRecord r) {
        String data = nullSafe(r.getAllergy())
                + "|" + nullSafe(r.getDiagnosis())
                + "|" + nullSafe(r.getNotes());
        return HashUtil.sha256(data);
    }

    /**
     * Builds the hash input string for MedicalServices (all service fields).
     */
    private String computeMedicalServicesHash(MedicalServices s) {
        String data = nullSafe(s.getHospiceAgency())
                + "|" + nullSafe(s.getPreferredHospital())
                + "|" + nullSafe(s.getPreferredPharmacy())
                + "|" + nullSafe(s.getHomeHealthAgency())
                + "|" + nullSafe(s.getMortuary())
                + "|" + nullSafe(s.getDnrPolst())
                + "|" + nullSafe(String.valueOf(s.getHospice()))
                + "|" + nullSafe(String.valueOf(s.getHomeHealth()))
                + "|" + nullSafe(s.getNotes());
        return HashUtil.sha256(data);
    }

    // -------------------------------------------------------------------------
    // Hash update — call these whenever you save/update an entity
    // -------------------------------------------------------------------------

    /**
     * Recomputes and stores the hash on a MedicalProfile.
     * Call this after setting any field on the profile, before saving.
     */
    public void updateMedicalProfileHash(MedicalProfile profile) {
        profile.setDataHash(computeMedicalProfileHash(profile));
    }

    /**
     * Recomputes and stores the hash on a MedicalRecord.
     * Call this after setting any field on the record, before saving.
     */
    public void updateMedicalRecordHash(MedicalRecord record) {
        record.setDataHash(computeMedicalRecordHash(record));
    }

    /**
     * Recomputes and stores the hash on a MedicalServices.
     * Call this after setting any field on the services, before saving.
     */
    public void updateMedicalServicesHash(MedicalServices services) {
        services.setDataHash(computeMedicalServicesHash(services));
    }

    // -------------------------------------------------------------------------
    // Integrity verification — called inside every read method
    // -------------------------------------------------------------------------

    /**
     * Verifies that a MedicalProfile's stored hash matches its current field values.
     * Throws SecurityException if the data appears to have been tampered with.
     */
    private void verifyMedicalProfileIntegrity(MedicalProfile profile) {
        if (profile.getDataHash() == null) {
            // record was created before hashing was introduced — skip check
            return;
        }
        String expected = computeMedicalProfileHash(profile);
        if (!HashUtil.verify(expected, profile.getDataHash())) {
            throw new SecurityException(
                    "Data integrity check failed for MedicalProfile of resident ID: "
                            + profile.getResidentId()
            );
        }
    }

    /**
     * Verifies that a MedicalRecord's stored hash matches its current field values.
     */
    private void verifyMedicalRecordIntegrity(MedicalRecord record) {
        if (record.getDataHash() == null) {
            return;
        }
        String expected = computeMedicalRecordHash(record);
        if (!HashUtil.verify(expected, record.getDataHash())) {
            throw new SecurityException(
                    "Data integrity check failed for MedicalRecord of resident ID: "
                            + record.getResidentId()
            );
        }
    }

    /**
     * Verifies that a MedicalServices's stored hash matches its current field values.
     */
    private void verifyMedicalServicesIntegrity(MedicalServices services) {
        if (services.getDataHash() == null) {
            return;
        }
        String expected = computeMedicalServicesHash(services);
        if (!HashUtil.verify(expected, services.getDataHash())) {
            throw new SecurityException(
                    "Data integrity check failed for MedicalServices of resident ID: "
                            + services.getResidentId()
            );
        }
    }

    // -------------------------------------------------------------------------
    // Medical Profile reads
    // -------------------------------------------------------------------------

    // get resident insurance info
    public String getInsurance(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        verifyMedicalProfileIntegrity(medicalProfile);

        return medicalProfile.getInsurance();
    }

    // get notes from medical profile
    public String getMedicalProfileNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        verifyMedicalProfileIntegrity(medicalProfile);

        return medicalProfile.getNotes();
    }

    // get capability info (mobility, cognitive status, etc)
    // Note: Capability has no sensitive free-text fields, so no hash is needed here.
    public CapabilityDTO getCapability(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        return CapabilityDTO.fromEntity(medicalProfile.getCapability());
    }

    // -------------------------------------------------------------------------
    // Medical Record reads
    // -------------------------------------------------------------------------

    // get all allergies for resident
    public List<Map<String, Object>> getAllAllergies(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        if (medicalRecord == null) {
            return Collections.emptyList();
        }

        verifyMedicalRecordIntegrity(medicalRecord);

        // convert list of allergies to list of maps for json response
        List<String> allergies = medicalRecord.getAllergyList();
        return allergies.stream()
                .map(allergy -> Map.of("allergy", (Object) allergy))
                .collect(Collectors.toList());
    }

    // get all diagnoses for resident
    public List<Map<String, Object>> getAllDiagnoses(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        if (medicalRecord == null) {
            return Collections.emptyList();
        }

        verifyMedicalRecordIntegrity(medicalRecord);

        List<String> diagnoses = medicalRecord.getDiagnosisList();
        return diagnoses.stream()
                .map(diagnosis -> Map.of("diagnosis", (Object) diagnosis))
                .collect(Collectors.toList());
    }

    // get medical record notes
    public String getMedicalRecordNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        if (medicalRecord != null) {
            verifyMedicalRecordIntegrity(medicalRecord);
        }

        return medicalRecord != null ? medicalRecord.getNotes() : null;
    }

    // -------------------------------------------------------------------------
    // Medical Services reads
    // -------------------------------------------------------------------------

    // get hospice agency name
    public String getHospiceAgency(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getHospiceAgency() : null;
    }

    // get preferred hospital
    public String getPreferredHospital(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getPreferredHospital() : null;
    }

    // get preferred pharmacy
    public String getPreferredPharmacy(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getPreferredPharmacy() : null;
    }

    // get home health agency
    public String getPreferredHomeHealthAgency(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getHomeHealthAgency() : null;
    }

    // get mortuary info
    public String getMortuary(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getMortuary() : null;
    }

    // get DNR/POLST status
    public String getDNRPolst(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getDnrPolst() : null;
    }

    // check if resident is on hospice
    public Boolean getHospice(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getHospice() : null;
    }

    // check if resident gets home health services
    public Boolean getHomeHealth(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getHomeHealth() : null;
    }

    // get medical services notes
    public String getMedicalServiceNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalServices medicalServices = getMedicalServices(residentId);
        if (medicalServices != null) verifyMedicalServicesIntegrity(medicalServices);

        return medicalServices != null ? medicalServices.getNotes() : null;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    // find user by ID
    private UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    // find medical profile by resident ID
    private MedicalProfile getMedicalProfileByResidentId(Long residentId) {
        return medicalProfileRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Medical profile not found for resident ID: " + residentId));
    }

    // convenience helper to get MedicalServices through the profile
    private MedicalServices getMedicalServices(Long residentId) {
        return getMedicalProfileByResidentId(residentId).getMedicalServices();
    }

    // treat null fields as empty string so hash input is always well-defined
    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    // check permissions
    private void validatePermissions(UserProfile currentUser, Long residentId) {
        // admin and caregiver can access any resident's medical info
        if (currentUser.getRole() == UserProfile.Role.ADMIN ||
                currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            return;
        }

        // residents can only see their own stuff
        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
                currentUser.getId().equals(residentId)) {
            return;
        }

        throw new IllegalArgumentException("You do not have permission to access this medical information");
    }
}