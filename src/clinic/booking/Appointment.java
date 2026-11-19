package clinic.booking;

import clinic.Validation;
import clinic.staff.HealthProfessional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * One booking: a patient, a health professional, and the moment they meet.
 *
 * <p><b>What an appointment stores.</b> The patient is modelled as two fields
 * (name and mobile number) rather than as a separate class. In this system a
 * patient has no identity beyond a booking - nothing is looked up by patient,
 * nothing is shared between bookings, and no patient history is kept - so a
 * {@code Patient} class would add an indirection without adding behaviour. If
 * patients ever needed a record of their own (history, Medicare number, a list
 * of their appointments), extracting the class would be a purely local change.</p>
 *
 * <p><b>Why the fields are final.</b> An appointment is a statement about the
 * past or the near future: "this patient is seeing this professional at this
 * time". Changing any part of it would silently create a different booking, so
 * the object is immutable - to move an appointment the caller cancels it and
 * books again. Immutability also makes the object safe to hand out from
 * {@link AppointmentManager} without defensive copying.</p>
 *
 * <p><b>Why the type of professional is the base class.</b> The field is
 * declared as {@link HealthProfessional}, so one appointment class works with a
 * GP, a specialist, a surgeon, a dietitian or a nurse without any
 * {@code if/else} on the professional's type. Printing the appointment then
 * shows the professional's own details, because the object prints itself.</p>
 *
 * <p><b>Why {@link LocalDate} and {@link LocalTime}.</b> Storing the time as
 * {@code java.time} values (instead of, say, a {@code String} such as
 * {@code "9:30"}) means comparisons are chronological and correct, and the
 * natural ordering needed for {@link #compareTo(Appointment)} is already
 * implemented. A string would sort {@code "10:00"} before {@code "9:00"}.</p>
 *
 * <p><b>Validity.</b> The constructor rejects missing or malformed data
 * immediately, so an {@code Appointment} that exists is always complete: a real
 * patient name, a usable phone number, a professional, a date and a time. Rules
 * that depend on the clinic's timetable or on the other bookings (slot
 * boundaries, opening hours, double bookings) belong to
 * {@link AppointmentManager}, because a single appointment does not know what
 * else is in the day's schedule.</p>
 */
public final class Appointment implements Comparable<Appointment> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String patientName;
    private final String patientPhone;
    private final HealthProfessional professional;
    private final LocalDate date;
    private final LocalTime time;

    /**
     * Creates a complete appointment.
     *
     * @param patientName  the patient's full name, must not be blank
     * @param patientPhone the patient's mobile number, 8 to 15 digits
     *                     (spaces and hyphens are allowed and removed)
     * @param professional the professional the patient is seeing
     * @param date         the day of the appointment
     * @param time         the start time; seconds are ignored because the clinic
     *                     works to the minute
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Appointment(String patientName, String patientPhone,
                       HealthProfessional professional, LocalDate date, LocalTime time) {
        this.patientName = Validation.text(patientName, "Patient name");
        this.patientPhone = Validation.phone(patientPhone, "Patient phone number");
        this.professional = Validation.required(professional, "A health professional");
        this.date = Validation.required(date, "An appointment date");
        this.time = Validation.required(time, "An appointment time").withSecond(0).withNano(0);
    }

    /**
     * @return the patient's full name
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * @return the patient's phone number in stored form (digits only)
     */
    public String getPatientPhone() {
        return patientPhone;
    }

    /**
     * @return the professional this appointment is with
     */
    public HealthProfessional getProfessional() {
        return professional;
    }

    /**
     * @return the day of the appointment
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return the start time of the appointment
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * Orders appointments the way the clinic reads its day: by date, then by
     * start time. Two appointments at the same time (with different
     * professionals) are ordered by professional id only to make the order
     * stable and repeatable - the clinic does not care which of them comes
     * first.
     *
     * <p>Implementing {@link Comparable} on the appointment itself means any
     * collection of appointments can be sorted with
     * {@code Collections.sort(...)} without a separate {@code Comparator},
     * which is why {@link AppointmentManager} can sort both full and filtered
     * lists with one line.</p>
     *
     * @param other the appointment to compare with
     * @return a negative number, zero or a positive number as this appointment
     *         is earlier than, equal in the ordering to, or later than {@code other}
     */
    @Override
    public int compareTo(Appointment other) {
        int byDate = date.compareTo(other.date);
        if (byDate != 0) {
            return byDate;
        }
        int byTime = time.compareTo(other.time);
        if (byTime != 0) {
            return byTime;
        }
        return professional.getId().compareTo(other.professional.getId());
    }

    /**
     * Prints the whole appointment, including the professional's own details, on
     * two lines so that a list of appointments stays readable. The second line is
     * indented, so it reads as part of the appointment above it.
     *
     * @return a readable description of this appointment
     */
    @Override
    public String toString() {
        return String.format("%s %s | patient: %s (%s)%n        with %s",
                date.format(DATE_FORMAT), time.format(TIME_FORMAT),
                patientName, patientPhone, professional);
    }
}
