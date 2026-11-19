package clinic.staff;

import clinic.Validation;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * A surgeon: a doctor who performs operations in the clinic's operating theatre
 * in addition to seeing patients in consultation.
 *
 * <p>On top of the inherited prescription capability this role adds its own
 * behaviour - {@link #bookTheatre(String)} - because only a surgeon can reserve
 * operating time. Behaviour that only one role supports belongs in that role,
 * not in the base class.</p>
 */
public class Surgeon extends HealthProfessional implements Prescriber {

    private final String surgicalField;
    private final String homeTheatre;
    private final boolean onEmergencyRoster;

    /**
     * Creates a surgeon with a specific roster.
     *
     * @param id               numeric identifier (digits only)
     * @param name             full name
     * @param workingDays      the days this surgeon is in the clinic
     * @param surgicalField    the area operated on, e.g. {@code "Orthopaedic surgery"}
     * @param homeTheatre      the theatre this surgeon usually works in
     * @param onEmergencyRoster whether this surgeon can be called for emergency cases
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Surgeon(String id, String name, Set<DayOfWeek> workingDays,
                   String surgicalField, String homeTheatre, boolean onEmergencyRoster) {
        super(id, name, workingDays);
        this.surgicalField = Validation.text(surgicalField, "Surgical field");
        this.homeTheatre = Validation.text(homeTheatre, "Home theatre");
        this.onEmergencyRoster = onEmergencyRoster;
    }

    /**
     * Convenience constructor for a surgeon who works Monday to Friday in
     * Theatre 1 and is not on the emergency roster.
     *
     * @param id            numeric identifier (digits only)
     * @param name          full name
     * @param surgicalField the area operated on
     */
    public Surgeon(String id, String name, String surgicalField) {
        this(id, name, STANDARD_WEEK, surgicalField, "Theatre 1", false);
    }

    /**
     * @return the surgical field of this surgeon
     */
    public String getSurgicalField() {
        return surgicalField;
    }

    /**
     * @return {@code true} if this surgeon can be called for emergency operations
     */
    public boolean isOnEmergencyRoster() {
        return onEmergencyRoster;
    }

    @Override
    public String getRole() {
        return "Surgeon (" + surgicalField + ")";
    }

    @Override
    protected String describeSpecialisation() {
        return String.format("field: %s, theatre: %s, emergency roster: %s, can prescribe",
                surgicalField, homeTheatre, onEmergencyRoster ? "yes" : "no");
    }

    @Override
    public String prescribe(String medication, String patientName) {
        return String.format("%s [%s, ID %s] prescribes %s for %s.",
                getName(), getRole(), getId(), medication, patientName);
    }

    /**
     * Reserves operating time for an operation.
     *
     * @param operation the operation to be performed
     * @return a one-line confirmation
     */
    public String bookTheatre(String operation) {
        return String.format("%s [%s, ID %s] reserves %s for: %s.",
                getName(), getRole(), getId(), homeTheatre, operation);
    }
}
