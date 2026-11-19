package clinic.staff;

import clinic.Validation;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * A dietitian: advises patients on nutrition. A dietitian is a health
 * professional at the clinic, but - unlike the doctors - is <b>not</b> a
 * {@link Prescriber}. This is exactly why prescribing is modelled as an
 * interface instead of a method on the base class.
 */
public class Dietitian extends HealthProfessional {

    private final String dietaryFocus;
    private final boolean bulkBilled;

    /**
     * Creates a dietitian with a specific roster.
     *
     * @param id            numeric identifier (digits only)
     * @param name          full name
     * @param workingDays   the days this dietitian is in the clinic
     * @param dietaryFocus  the area of nutrition this dietitian specialises in
     * @param bulkBilled    whether appointments are bulk billed
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Dietitian(String id, String name, Set<DayOfWeek> workingDays,
                     String dietaryFocus, boolean bulkBilled) {
        super(id, name, workingDays);
        this.dietaryFocus = Validation.text(dietaryFocus, "Dietary focus");
        this.bulkBilled = bulkBilled;
    }

    /**
     * Convenience constructor for a Monday to Friday dietitian in general
     * nutrition who bulk bills.
     *
     * @param id   numeric identifier (digits only)
     * @param name full name
     */
    public Dietitian(String id, String name) {
        this(id, name, STANDARD_WEEK, "general nutrition", true);
    }

    /**
     * @return the nutrition area this dietitian focuses on
     */
    public String getDietaryFocus() {
        return dietaryFocus;
    }

    /**
     * @return {@code true} if appointments are bulk billed
     */
    public boolean isBulkBilled() {
        return bulkBilled;
    }

    @Override
    public String getRole() {
        return "Dietitian";
    }

    @Override
    protected String describeSpecialisation() {
        return String.format("focus: %s, bulk billed: %s, cannot prescribe",
                dietaryFocus, bulkBilled ? "yes" : "no");
    }

    /**
     * Reviews the diet plan of a patient.
     *
     * @param patientName the patient whose plan is reviewed
     * @return a one-line record of the review
     */
    public String reviewDiet(String patientName) {
        return String.format("%s [%s, ID %s] reviews the diet plan of %s (focus: %s).",
                getName(), getRole(), getId(), patientName, dietaryFocus);
    }
}
