═══════════════════════════════════════════════════════════════════════
                        CLINICORE DATABASE STRUCTURE
═══════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────┐
│                      1. USER MANAGEMENT HIERARCHY                    │
└─────────────────────────────────────────────────────────────────────┘

                         ┌─────────────────────┐
                         │   USER_PROFILE      │
                         │   (Base Entity)     │
                         ├─────────────────────┤
                         │ PK: id              │
                         │ UK: username        │
                         │     first_name      │
                         │     last_name       │
                         │     gender          │
                         │     birthday        │
                         │     contact_number  │
                         │     password_hash   │
                         │     created_at      │
                         │     updated_at      │
                         └──────────┬──────────┘
                                    │
                     ┌──────────────┼──────────────┐
                     │              │              │
           ┌─────────▼──────┐  ┌───▼──────────┐  ┌▼──────────┐
           │     ADMIN      │  │  CAREGIVER   │  │ RESIDENT  │
           ├────────────────┤  ├──────────────┤  ├───────────┤
           │ PK/FK: id      │  │ PK/FK: id    │  │PK/FK: id  │
           │ (inherits all) │  │     notes    │  │emergency_ │
           └────────────────┘  │ (inherits)   │  │contact_   │
                               └──────┬───────┘  │name       │
                                      │          │emergency_ │
                                      │          │contact_   │
                                      │          │number     │
                                      │          │notes      │
                                      │          │created_at │
                                      │          │updated_at │
                                      │          └─────┬─────┘
                                      │                │
                                      └────────┬───────┘
                                               │
                         ┌─────────────────────▼─────────────────┐
                         │   RESIDENT_CAREGIVER (Junction)       │
                         ├───────────────────────────────────────┤
                         │ PK: (resident_id, caregiver_id)       │
                         │ FK: resident_id → RESIDENT.id         │
                         │ FK: caregiver_id → CAREGIVER.id       │
                         │     assigned_at                       │
                         └───────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                   2. COMMUNICATION SYSTEM                            │
└─────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────────────┐
                    │  COMMUNICATION_PORTAL       │
                    ├─────────────────────────────┤
                    │ PK: id (auto-increment)     │
                    │ FK: sender_id    ───────────┼──┐
                    │     sender_role (enum)      │  │
                    │ FK: recipient_id ───────────┼──┼──► USER_PROFILE.id
                    │     recipient_role (enum)   │  │
                    │     subject                 │  │
                    │     message (TEXT)          │  │
                    │     sent_at                 │  │
                    │     read_at (nullable)      │  │
                    │     is_read (boolean)       │  │
                    └─────────────────────────────┘  │
                                                     │
                    ┌────────────────────────────────┘
                    └──► (Self-referencing to USER_PROFILE)

Roles: ADMIN, CAREGIVER, RESIDENT

┌─────────────────────────────────────────────────────────────────────┐
│               3. RESIDENT MEDICAL INFORMATION SYSTEM                 │
└─────────────────────────────────────────────────────────────────────┘

                         ┌──────────────┐
                         │   RESIDENT   │
                         └──────┬───────┘
                                │
                                │ 1:1
                                ▼
                    ┌───────────────────────┐
                    │  MEDICAL_PROFILE      │
                    ├───────────────────────┤
                    │ PK/FK: resident_id    │
                    │        insurance      │
                    │        notes          │
                    │        created_at     │
                    │        updated_at     │
                    └───────┬───────────────┘
                            │
            ┌───────────────┼───────────────┬──────────────┐
            │ 1:1           │ 1:1           │ 1:1          │ 1:N
            ▼               ▼               ▼              ▼
    ┌───────────┐  ┌────────────────┐  ┌──────────┐  ┌────────────┐
    │CAPABILITY │  │ MEDICAL_       │  │ MEDICAL_ │  │ MEDICATION │
    ├───────────┤  │ SERVICES       │  │ RECORD   │  ├────────────┤
    │PK/FK:     │  ├────────────────┤  ├──────────┤  │PK: id      │
    │resident_id│  │PK/FK:          │  │PK/FK:    │  │FK: medical_│
    │           │  │resident_id     │  │resident_ │  │    profile_│
    │verbal     │  │                │  │id        │  │    id      │
    │self_      │  │hospice_agency  │  │          │  │FK: medica- │
    │medicates  │  │preferred_      │  │allergy   │  │    tion_   │
    │           │  │hospital        │  │(TEXT)    │  │    inven-  │
    │inconti-   │  │preferred_      │  │diagnosis │  │    tory_id │
    │nence_     │  │pharmacy        │  │(TEXT)    │  │(nullable)  │
    │status     │  │home_health_    │  │notes     │  │            │
    │(enum)     │  │agency          │  │(TEXT)    │  │medication_ │
    │           │  │mortuary        │  │created_at│  │name        │
    │mobility_  │  │dnr_polst       │  │updated_at│  │dosage      │
    │status     │  │hospice (bool)  │  └──────────┘  │frequency   │
    │(enum)     │  │home_health     │                │intake_     │
    └───────────┘  │(bool)          │                │status      │
                   │notes           │                │(enum)      │
                   └────────────────┘                │last_admin  │
                                                     │istered_at  │
                                                     │notes       │
                                                     │created_at  │
                                                     │updated_at  │
                                                     └──────┬─────┘
                                                            │
                                                            │ N:1
                                                            │ (optional)
                                                            ▼
                                                  ┌──────────────────┐
                                                  │  MEDICATION_     │
                                                  │  INVENTORY       │
                                                  ├──────────────────┤
                                                  │ PK/FK: id        │
                                                  │ dosage_per_      │
                                                  │ serving          │
                                                  │ notes (TEXT)     │
                                                  └──────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    4. DOCUMENT MANAGEMENT                            │
└─────────────────────────────────────────────────────────────────────┘

        ┌──────────────┐
        │   RESIDENT   │
        └──────┬───────┘
               │
               │ 1:N
               ▼
        ┌─────────────────┐
        │   DOCUMENTS     │
        ├─────────────────┤
        │ PK: id          │
        │ FK: resident_id │
        │     title       │
        │     document    │
        │     (LONGBLOB)  │
        │     type        │
        │     uploaded_at │
        └─────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                  5. INVENTORY MANAGEMENT SYSTEM                      │
└─────────────────────────────────────────────────────────────────────┘

            ┌─────────────────┐
            │    SUPPLIER     │
            ├─────────────────┤
            │ PK: id          │
            │     name        │
            │     phone_      │
            │     number      │
            │     address     │
            │     notes       │
            │     created_at  │
            │     updated_at  │
            └────────┬────────┘
                     │
                     │ 1:N
                     ▼
            ┌─────────────────┐
            │      ITEM       │
            │  (Base Entity)  │
            ├─────────────────┤
            │ PK: id          │
            │ FK: supplier_id │
            │     (nullable)  │
            │     name        │
            │     quantity    │
            │     created_at  │
            │     updated_at  │
            └────────┬────────┘
                     │
            ┌────────┴─────────┐
            │ (Inheritance)    │
            │                  │
            ▼                  ▼
    ┌──────────────┐   ┌──────────────────┐
    │  MEDICAL_    │   │  MEDICATION_     │
    │  CONSUMABLES │   │  INVENTORY       │
    ├──────────────┤   ├──────────────────┤
    │ PK/FK: id    │   │ PK/FK: id        │
    │ (inherits)   │   │ dosage_per_      │
    └──────────────┘   │ serving          │
                       │ notes            │
                       └──────────────────┘
                                ▲
                                │
                                │ N:1 (optional)
                                │
                       ┌────────┴─────────┐
                       │    MEDICATION    │
                       │ (references this)│
                       └──────────────────┘
