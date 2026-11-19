package clinic.staff;

import clinic.Validation;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * A specialist: a doctor who has completed advanced training in one field of
 * medicine, such as paediatrics, radiology or cardiology.
 *
 * <p>The specialty is what makes this role different from a GP, so the role name
 * itself reports it ({@code "Specialist (Paediatrics)"}). This keeps the type
 * visible in any list of staff without the caller having to remember what kind
 * of object it is holding.</p>
 */
public class Specialist extends HealthProfessional implements Prescriber {

    private final String specialty;
    private final String consultingHospital;

    /**
     * Creates a specialist with a specific roster.
     *
     * @param id                 numeric identifier (digits only)
     * @param name               full name
     * @param workingDays        the days this specialist is in the clinic
     * @param specialty          the field of medicine, e.g. {@code "Paediatrics"}
     * @param consultingHospital the hospital this specialist also visits
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Specialist(String id, String name, Set<DayOfWeek> workingDays,
                      String specialty, String consultingHospital) {
        super(id, name, workingDays);
        this.specialty = Validation.text(specialty, "Specialty");
        this.consultingHospital = Validation.text(consultingHospital, "Consulting hospital");
    }

    /**
     * Convenience constructor for a specialist who works the standard Monday to
     * Friday week and consults only at this clinic.
     *
     * @param id        numeric identifier (digits only)
     * @param name      full name
     * @param specialty the field of medicine, e.g. {@code "Cardiology"}
     */
    public Specialist(String id, String name, String specialty) {
        this(id, name, STANDARD_WEEK, specialty, "this clinic");
    }

    /**
     * @return the specialty of this doctor
     */
    public String getSpecialty() {
        return specialty;
    }

    /**
     * @return the hospital this specialist also consults at
     */
    public String getConsultingHospital() {
        return consultingHospital;
    }

    @Override
    public String getRole() {
        return "Specialist (" + specialty + ")";
    }

    @Override
    protected String describeSpecialisation() {
        return String.format("specialty: %s, also consults at %s, can prescribe",
                specialty, consultingHospital);
    }

    @Override
    public String prescribe(String medication, String patientName) {
        return String.format("%s [%s, ID %s] prescribes %s for %s.",
                getName(), getRole(), getId(), medication, patientName);
    }
}
