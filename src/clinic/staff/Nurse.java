package clinic.staff;

import clinic.Validation;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * A nurse: takes observations, administers treatment and - when authorised -
 * collects blood samples.
 *
 * <p>A nurse cannot prescribe, so this class does not implement
 * {@link Prescriber}. The authorisation flag is checked every time
 * {@link #takeBloodSample(String)} is called, so the object can never claim a
 * procedure it is not qualified to perform.</p>
 */
public class Nurse extends HealthProfessional {

    private final String nursingLevel;
    private final boolean authorisedForBloodCollection;

    /**
     * Creates a nurse with a specific roster.
     *
     * @param id                          numeric identifier (digits only)
     * @param name                        full name
     * @param workingDays                 the days this nurse is in the clinic
     * @param nursingLevel                the qualification level, e.g. {@code "Registered Nurse"}
     * @param authorisedForBloodCollection whether this nurse may collect blood samples
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Nurse(String id, String name, Set<DayOfWeek> workingDays,
                 String nursingLevel, boolean authorisedForBloodCollection) {
        super(id, name, workingDays);
        this.nursingLevel = Validation.text(nursingLevel, "Nursing level");
        this.authorisedForBloodCollection = authorisedForBloodCollection;
    }

    /**
     * Convenience constructor for a Monday to Friday registered nurse who is
     * authorised to collect blood samples.
     *
     * @param id   numeric identifier (digits only)
     * @param name full name
     */
    public Nurse(String id, String name) {
        this(id, name, STANDARD_WEEK, "Registered Nurse", true);
    }

    /**
     * @return the qualification level of this nurse
     */
    public String getNursingLevel() {
        return nursingLevel;
    }

    /**
     * @return {@code true} if this nurse may collect blood samples
     */
    public boolean isAuthorisedForBloodCollection() {
        return authorisedForBloodCollection;
    }

    @Override
    public String getRole() {
        return "Nurse";
    }

    @Override
    protected String describeSpecialisation() {
        return String.format("level: %s, blood collection: %s, cannot prescribe",
                nursingLevel, authorisedForBloodCollection ? "authorised" : "not authorised");
    }

    /**
     * Collects a blood sample from a patient.
     *
     * @param patientName the patient the sample is taken from
     * @return a one-line record of the procedure, or a refusal when this nurse
     *         is not authorised
     */
    public String takeBloodSample(String patientName) {
        if (!authorisedForBloodCollection) {
            return String.format("%s [%s, ID %s] is not authorised to collect blood, so %s is referred to a colleague.",
                    getName(), getRole(), getId(), patientName);
        }
        return String.format("%s [%s, ID %s] collects a blood sample from %s.",
                getName(), getRole(), getId(), patientName);
    }
}
