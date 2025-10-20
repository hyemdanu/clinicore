import jakarta.persistence.*;

@Entity
@Table(name = "capabilities")
public class Capabilities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AnswerOption isVerbal;

    @Enumerated(EnumType.STRING)
    private AnswerOption selfMedicines;

    @Enumerated(EnumType.STRING)
    private IncontinenceStatus incontinenceStatus;

    @Enumerated(EnumType.STRING)
    private MobilityCapability mobilityCapability;

    // ---- Constructors ----
    public Capabilities() {}

    public Capabilities(AnswerOption isVerbal, AnswerOption selfMedicines,
                        IncontinenceStatus incontinenceStatus, MobilityCapability mobilityCapability) {
        this.isVerbal = isVerbal;
        this.selfMedicines = selfMedicines;
        this.incontinenceStatus = incontinenceStatus;
        this.mobilityCapability = mobilityCapability;
    }

    // ---- Getters and Setters ----
    public Long getId() { return id; }

    public AnswerOption getIsVerbal() { return isVerbal; }
    public void setIsVerbal(AnswerOption isVerbal) { this.isVerbal = isVerbal; }

    public AnswerOption getSelfMedicines() { return selfMedicines; }
    public void setSelfMedicines(AnswerOption selfMedicines) { this.selfMedicines = selfMedicines; }

    public IncontinenceStatus getIncontinenceStatus() { return incontinenceStatus; }
    public void setIncontinenceStatus(IncontinenceStatus incontinenceStatus) { this.incontinenceStatus = incontinenceStatus; }

    public MobilityCapability getMobilityCapability() { return mobilityCapability; }
    public void setMobilityCapability(MobilityCapability mobilityCapability) { this.mobilityCapability = mobilityCapability; }
}
