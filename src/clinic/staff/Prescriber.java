package clinic.staff;

/**
 * Capability implemented by every professional who is allowed to write a
 * prescription.
 *
 * <p>Prescribing is <em>not</em> a property of "being a health professional"
 * (a dietitian cannot prescribe), and it is not a property of "being a doctor"
 * either - it is a separate <b>capability</b>. Modelling it as an interface
 * rather than as a method on {@link HealthProfessional} means:</p>
 *
 * <ul>
 *   <li>the base class stays free of behaviour that only some roles support;</li>
 *   <li>code that needs to prescribe can accept {@code Prescriber} and the
 *       compiler guarantees the operation is legal;</li>
 *   <li>a future role (a pharmacist, say) can become a prescriber without
 *       touching the existing hierarchy.</li>
 * </ul>
 */
public interface Prescriber {

    /**
     * Issues a prescription for a patient.
     *
     * @param medication  the drug and dose, e.g. {@code "amoxicillin 500 mg"}
     * @param patientName the patient who will take the medication
     * @return a one-line record of the prescription, ready to be printed
     */
    String prescribe(String medication, String patientName);
}
