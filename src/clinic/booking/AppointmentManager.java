package clinic.booking;

import clinic.Validation;
import clinic.staff.HealthProfessional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Owns every appointment the clinic has accepted, and is the single place where
 * the booking rules are enforced.
 *
 * <p><b>Why the appointments live in a {@code Map}.</b> The clinic's most
 * frequent question is "is this professional free at this time?", and the clinic's
 * most important rule is "the same professional is never booked twice in the same
 * slot". Both are answered in constant time by a map keyed on
 * <em>professional id + date + time</em>, and the key makes the second booking of
 * a slot impossible: it would overwrite the first one, which the duplicate check
 * rejects before it can happen. The map is a {@link LinkedHashMap} so that
 * appointments also come back out in the order they were made, which keeps the
 * printed output of the demo predictable before it is sorted.</p>
 *
 * <p>A plain list would have to scan every appointment to detect a clash, and a
 * {@code Set} of appointments would not help because two appointments for
 * different patients in different slots are different objects - the uniqueness
 * rule is about the (professional, time) pair, not about the appointment
 * itself.</p>
 *
 * <p><b>Why every operation returns a message.</b> The clinic's staff need
 * feedback, and a caller in the {@code main} method must be able to show both
 * the successful and the refused cases. Returning a short, already formatted
 * sentence keeps the presentation logic out of this class while making failure
 * impossible to ignore - a {@code boolean} would let a caller lose the reason
 * for the refusal.</p>
 */
public class AppointmentManager {

    /** Prefix of a message that reports a successful booking. */
    private static final String BOOKED = "BOOKED    ";

    /** Prefix of a message that reports a refused operation. */
    private static final String REJECTED = "REJECTED  ";

    /** Prefix of a message that reports a successful cancellation. */
    private static final String CANCELLED = "CANCELLED ";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ClinicHours clinicHours;

    /**
     * Every accepted appointment, keyed by {@code professional id + date + time}.
     */
    private final Map<String, Appointment> appointments = new LinkedHashMap<>();

    /**
     * Creates a manager for the clinic's standard 08:00-16:00 timetable.
     */
    public AppointmentManager() {
        this(new ClinicHours());
    }

    /**
     * Creates a manager for a custom timetable.
     *
     * @param clinicHours the timetable to enforce
     * @throws IllegalArgumentException if {@code clinicHours} is {@code null}
     */
    public AppointmentManager(ClinicHours clinicHours) {
        this.clinicHours = Validation.required(clinicHours, "Clinic hours");
    }

    /**
     * @return the timetable this manager enforces
     */
    public ClinicHours getClinicHours() {
        return clinicHours;
    }

    /**
     * Books an appointment for today.
     *
     * @param patientName  the patient's name
     * @param patientPhone the patient's phone number
     * @param professional the professional to see, of any concrete role
     * @param time         the start time; must be one of the clinic's slots
     * @return a message describing the outcome, never {@code null}
     */
    public String book(String patientName, String patientPhone,
                       HealthProfessional professional, LocalTime time) {
        return book(patientName, patientPhone, professional, LocalDate.now(), time);
    }

    /**
     * Books an appointment, enforcing every rule of the clinic.
     *
     * <p>The order of the checks matters for the quality of the feedback: the
     * appointment object is built first, so malformed patient data is refused
     * before any clinic rule is examined.</p>
     *
     * @param patientName  the patient's name
     * @param patientPhone the patient's phone number
     * @param professional the professional to see, of any concrete role
     * @param date         the day of the appointment; the clinic only books today
     * @param time         the start time; must be one of the clinic's slots
     * @return a message describing the outcome, never {@code null}
     */
    public String book(String patientName, String patientPhone,
                       HealthProfessional professional, LocalDate date, LocalTime time) {
        if (professional == null) {
            return REJECTED + "no health professional was selected.";
        }

        Appointment appointment;
        try {
            appointment = new Appointment(patientName, patientPhone, professional, date, time);
        } catch (IllegalArgumentException invalid) {
            return REJECTED + "incomplete booking - " + invalid.getMessage();
        }

        String ruleBroken = checkBookingRules(appointment);
        if (ruleBroken != null) {
            return REJECTED + ruleBroken;
        }

        appointments.put(slotKey(appointment), appointment);
        return String.format("%s%s  %s [%s, ID %s] -> %s (%s)",
                BOOKED,
                appointment.getTime().format(TIME_FORMAT),
                appointment.getProfessional().getName(),
                appointment.getProfessional().getRole(),
                appointment.getProfessional().getId(),
                appointment.getPatientName(),
                appointment.getPatientPhone());
    }

    /**
     * Applies the clinic's booking rules to a well-formed appointment.
     *
     * @param appointment the appointment that has passed object validation
     * @return the reason the booking must be refused, or {@code null} if the
     *         appointment may be accepted
     */
    private String checkBookingRules(Appointment appointment) {
        LocalDate today = LocalDate.now();
        if (!appointment.getDate().equals(today)) {
            return "appointments can only be made for today ("
                    + today.format(DATE_FORMAT) + ").";
        }

        if (!clinicHours.isSlot(appointment.getTime())) {
            return appointment.getTime().format(TIME_FORMAT)
                    + " is not an appointment slot; the clinic runs " + clinicHours.describe() + ".";
        }

        HealthProfessional professional = appointment.getProfessional();
        DayOfWeek day = appointment.getDate().getDayOfWeek();
        if (!professional.worksOn(day)) {
            return professional.getName() + " is not rostered on "
                    + day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    + " (roster: " + professional.getWorkingDaysAsText() + ").";
        }

        if (appointments.containsKey(slotKey(appointment))) {
            return professional.getName() + " is already booked at "
                    + appointment.getTime().format(TIME_FORMAT) + " on "
                    + appointment.getDate().format(DATE_FORMAT) + ".";
        }

        return null;
    }

    /**
     * @return every appointment, earliest first
     */
    public List<Appointment> getAllAppointments() {
        return sortedCopy(appointments.values());
    }

    /**
     * Finds the appointments of one professional, earliest first. Any concrete
     * role can be looked up, because the search only needs the inherited id.
     *
     * @param professionalId the numeric id of the professional
     * @return the matching appointments, earliest first; empty if there are none
     */
    public List<Appointment> getAppointmentsForProfessional(String professionalId) {
        String wanted = professionalId == null ? "" : professionalId.trim();
        List<Appointment> matches = new ArrayList<>();
        for (Appointment appointment : appointments.values()) {
            if (appointment.getProfessional().getId().equals(wanted)) {
                matches.add(appointment);
            }
        }
        return sortedCopy(matches);
    }

    /**
     * Finds the appointments of one patient by phone number. The number is
     * normalised first, so {@code "0433 111 222"} and {@code "0433111222"} find
     * the same bookings.
     *
     * @param patientPhone the phone number to search for
     * @return the matching appointments, earliest first; empty if there are none
     */
    public List<Appointment> getAppointmentsForPatient(String patientPhone) {
        String wanted = normalisePhone(patientPhone);
        List<Appointment> matches = new ArrayList<>();
        for (Appointment appointment : appointments.values()) {
            if (appointment.getPatientPhone().equals(wanted)) {
                matches.add(appointment);
            }
        }
        return sortedCopy(matches);
    }

    /**
     * Lists the slots that are still free for a professional today. This is the
     * operation that shows the value of generating the timetable from
     * {@link ClinicHours}: no slot time is written down anywhere in this class.
     *
     * @param professional the professional of any concrete role
     * @return the free start times, in chronological order; empty if the
     *         professional is not in the clinic today or is fully booked
     */
    public List<LocalTime> getAvailableSlots(HealthProfessional professional) {
        List<LocalTime> free = new ArrayList<>();
        if (professional == null) {
            return free;
        }
        LocalDate today = LocalDate.now();
        if (!professional.worksOn(today.getDayOfWeek())) {
            return free;
        }
        for (LocalTime slot : clinicHours.getSlots()) {
            if (!appointments.containsKey(slotKey(professional.getId(), today, slot))) {
                free.add(slot);
            }
        }
        return free;
    }

    /**
     * Cancels one appointment, identified by the professional and the start
     * time. This is the key the clinic's staff use when a patient phones about
     * "my 10:30 with Dr Chen".
     *
     * @param professionalId the numeric id of the professional
     * @param time           the start time of the appointment to cancel
     * @return a message describing the outcome, never {@code null}
     */
    public String cancel(String professionalId, LocalTime time) {
        if (professionalId == null || time == null) {
            return REJECTED + "cancelling needs both a professional id and a time.";
        }
        Appointment removed = appointments.remove(slotKey(professionalId.trim(), LocalDate.now(), time));
        if (removed == null) {
            return REJECTED + "no appointment found for professional " + professionalId.trim()
                    + " at " + time.format(TIME_FORMAT) + " on "
                    + LocalDate.now().format(DATE_FORMAT) + ".";
        }
        return String.format("%s%s  %s (%s) with %s [ID %s] removed from the schedule.",
                CANCELLED,
                removed.getTime().format(TIME_FORMAT),
                removed.getPatientName(),
                removed.getPatientPhone(),
                removed.getProfessional().getName(),
                removed.getProfessional().getId());
    }

    /**
     * Cancels every appointment belonging to one patient, identified by phone
     * number.
     *
     * @param patientPhone the patient's phone number
     * @return a message describing the outcome, never {@code null}
     */
    public String cancelAllForPatient(String patientPhone) {
        List<Appointment> matches = getAppointmentsForPatient(patientPhone);
        if (matches.isEmpty()) {
            return REJECTED + "no appointments found for patient phone " + patientPhone + ".";
        }
        for (Appointment appointment : matches) {
            appointments.remove(slotKey(appointment));
        }

        StringBuilder message = new StringBuilder(String.format("%s%d %s for %s cancelled: ",
                CANCELLED, matches.size(), matches.size() == 1 ? "appointment" : "appointments",
                normalisePhone(patientPhone)));
        for (int i = 0; i < matches.size(); i++) {
            if (i > 0) {
                message.append(", ");
            }
            message.append(matches.get(i).getTime().format(TIME_FORMAT))
                    .append(" with ")
                    .append(matches.get(i).getProfessional().getName());
        }
        return message.toString();
    }

    /**
     * @return the number of appointments currently held
     */
    public int size() {
        return appointments.size();
    }

    /**
     * Copies a collection of appointments and sorts it from the earliest to the
     * latest. Working on a copy keeps the internal map untouched and lets the
     * caller sort any subset it likes.
     *
     * @param source the appointments to sort
     * @return a new, chronologically ordered list
     */
    private static List<Appointment> sortedCopy(Collection<Appointment> source) {
        List<Appointment> result = new ArrayList<>(source);
        Collections.sort(result);
        return result;
    }

    /**
     * Builds the key that identifies one professional's slot.
     *
     * @param appointment the appointment to key
     * @return the slot key of the appointment
     */
    private static String slotKey(Appointment appointment) {
        return slotKey(appointment.getProfessional().getId(), appointment.getDate(), appointment.getTime());
    }

    /**
     * Builds the key that identifies one professional's slot. The time is
     * formatted to the minute so that {@code 09:30:00} and {@code 09:30:15} map
     * onto the same slot.
     *
     * @param professionalId the professional's id
     * @param date           the day of the appointment
     * @param time           the start time of the appointment
     * @return the slot key
     */
    private static String slotKey(String professionalId, LocalDate date, LocalTime time) {
        return professionalId + "@" + date + "@" + time.format(TIME_FORMAT);
    }

    /**
     * Removes spaces and hyphens so phone numbers entered in different styles
     * can be compared.
     *
     * @param phone the number as entered by the user
     * @return the number in stored form
     */
    private static String normalisePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[\\s-]", "");
    }
}
