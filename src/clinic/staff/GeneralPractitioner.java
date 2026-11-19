package clinic.staff;

import clinic.Validation;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * A general practitioner: the first point of contact for a patient, and the role
 * that refers people on to specialists when needed.
 *
 * <p>Only the general-practice specific data lives here - the clinical interest
 * and the years in practice. Everything else (id, name, roster, equality,
 * printing) is inherited from {@link HealthProfessional}, so no shared
 * information is duplicated.</p>
 */
public class GeneralPractitioner extends HealthProfessional implements Prescriber {

    private final String clinicalInterest;
    private final int yearsInPractice;

    /**
     * Creates a general practitioner with a specific roster.
     *
     * @param id              numeric identifier (digits only)
     * @param name            full name
     * @param workingDays     the days this GP is in the clinic
     * @param clinicalInterest the area this GP focuses on
     * @param yearsInPractice years of experience, must not be negative
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public GeneralPractitioner(String id, String name, Set<DayOfWeek> workingDays,
                               String clinicalInterest, int yearsInPractice) {
        super(id, name, workingDays);
        this.clinicalInterest = Validation.text(clinicalInterest, "Clinical interest");
        this.yearsInPractice = Validation.nonNegative(yearsInPractice, "Years in practice");
    }

    /**
     * Convenience constructor for a full-time GP (Monday to Friday) whose main
     * interest is general family medicine.
     *
     * @param id              numeric identifier (digits only)
     * @param name            full name
     * @param yearsInPractice years of experience, must not be negative
     */
    public GeneralPractitioner(String id, String name, int yearsInPractice) {
        this(id, name, STANDARD_WEEK, "general family medicine", yearsInPractice);
    }

    /**
     * @return the clinical interest of this GP, e.g. {@code "family medicine"}
     */
    public String getClinicalInterest() {
        return clinicalInterest;
    }

    /**
     * @return the years this GP has been in practice
     */
    public int getYearsInPractice() {
        return yearsInPractice;
    }

    @Override
    public String getRole() {
        return "General Practitioner";
    }

    @Override
    protected String describeSpecialisation() {
        return String.format("interest: %s, %d years in practice, can prescribe",
                clinicalInterest, yearsInPractice);
    }

    @Override
    public String prescribe(String medication, String patientName) {
        return String.format("%s [%s, ID %s] prescribes %s for %s.",
                getName(), getRole(), getId(), medication, patientName);
    }
}
