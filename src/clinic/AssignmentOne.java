package clinic;

import clinic.booking.Appointment;
import clinic.booking.AppointmentManager;
import clinic.staff.Dietitian;
import clinic.staff.GeneralPractitioner;
import clinic.staff.HealthProfessional;
import clinic.staff.Nurse;
import clinic.staff.Prescriber;
import clinic.staff.Specialist;
import clinic.staff.Surgeon;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Scripted demonstration of the clinic appointment model.
 *
 * <p>There is no user interface, no menu and no persistence: this class plays the
 * part of the clinic's staff by creating objects, calling their behaviour and
 * printing the result, so that every feature of the model can be seen in one run
 * of the program. The output is divided into sections that follow the six parts
 * of the assignment.</p>
 *
 * <p>Run with {@code java clinic.AssignmentOne} (see the README for the exact
 * compile and run commands).</p>
 */
public class AssignmentOne {

    // Locale.ENGLISH keeps the report identical on a machine with a non-English
    // default locale (the console here is Chinese, which would otherwise print
    // "星期四 19 十一月 2026").
    private static final DateTimeFormatter FULL_DATE =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final String RULE = "==============================================================================";

    /**
     * Program entry point.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        LocalDate today = LocalDate.now();
        DayOfWeek tomorrow = today.plusDays(1).getDayOfWeek();

        heading("HEALTH CLINIC - APPOINTMENT SYSTEM (SCRIPTED DEMONSTRATION)");
        System.out.println("  Demo day : " + today.format(FULL_DATE));
        System.out.println("  Policy   : appointments can only be made for today");

        // =====================================================================
        // PARTS 1-3: the health professionals who work at the clinic
        // =====================================================================
        subheading("PARTS 1-3: THE CLINIC'S HEALTH PROFESSIONALS");

        GeneralPractitioner gpChen = new GeneralPractitioner("1001", "Dr Alice Chen",
                roster(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                "family medicine and women's health", 12);
        GeneralPractitioner gpOkafor = new GeneralPractitioner("1002", "Dr Ben Okafor",
                roster(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                "chronic disease management", 7);
        // Convenience constructor: full-time GP, Monday to Friday, general family medicine.
        GeneralPractitioner gpMartin = new GeneralPractitioner("1003", "Dr Chloe Martin", 3);

        Specialist paediatrician = new Specialist("2001", "Dr Priya Raman",
                roster(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                "Paediatrics", "City Children's Hospital");
        Specialist cardiologist = new Specialist("2002", "Dr Marco Silva",
                roster(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                "Cardiology", "Riverside Heart Centre");

        Surgeon surgeon = new Surgeon("3001", "Dr Helen Wu",
                roster(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                "Orthopaedic surgery", "Theatre 1", true);

        Dietitian dietitian = new Dietitian("4001", "Ms Yara Ibrahim",
                roster(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                "paediatric nutrition and diabetes", true);

        Nurse nurseOnDuty = new Nurse("5001", "Nurse Kate Brown",
                roster(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                "Registered Nurse", true);
        // A locum who covers one day only - the day after today - so she is
        // deliberately NOT available for a booking made today.
        Nurse nurseOffDuty = new Nurse("5002", "Nurse Grace Tan",
                EnumSet.of(tomorrow), "Enrolled Nurse", false);

        // One heterogeneous collection, handled through the single common type:
        // no separate list per role, and no if/else to print them.
        List<HealthProfessional> staff = new ArrayList<>(Arrays.asList(
                gpChen, gpOkafor, gpMartin, paediatrician, cardiologist,
                surgeon, dietitian, nurseOnDuty, nurseOffDuty));

        System.out.println("  " + staff.size()
                + " professionals, all held in one List<HealthProfessional>:");
        for (HealthProfessional professional : staff) {
            // Each object prints itself; the subclass supplies its own details.
            System.out.println("    " + professional);
        }

        // =====================================================================
        // PART 2: the same request answered differently, depending on the role
        // =====================================================================
        subheading("PART 2: EACH ROLE RESPONDS IN ITS OWN WAY");
        String patient = "John Smith";
        for (HealthProfessional professional : staff) {
            if (professional instanceof Prescriber) {
                // Only the roles that really can prescribe expose this behaviour.
                System.out.println("  " + ((Prescriber) professional).prescribe("amoxicillin 500 mg", patient));
            } else if (professional instanceof Nurse) {
                System.out.println("  " + ((Nurse) professional).takeBloodSample(patient));
            } else if (professional instanceof Dietitian) {
                System.out.println("  " + ((Dietitian) professional).reviewDiet(patient));
            }
        }
        System.out.println("  " + surgeon.bookTheatre("knee arthroscopy"));

        // =====================================================================
        // PARTS 4-6: appointments
        // =====================================================================
        AppointmentManager manager = new AppointmentManager();

        subheading("PARTS 4-5: BOOKING APPOINTMENTS FOR TODAY");
        System.out.println("  Clinic hours: " + manager.getClinicHours().describe());
        System.out.println();

        // Different professional types are booked through the same method.
        book(manager, "Aiko Tanaka", "0455 909 090", nurseOnDuty, LocalTime.of(8, 30));
        book(manager, "John Smith", "0412 345 678", gpChen, LocalTime.of(9, 0));
        book(manager, "Mary Jones", "0433 111 222", gpChen, LocalTime.of(9, 30));
        book(manager, "Timmy Jones", "0433 111 222", paediatrician, LocalTime.of(10, 0));
        book(manager, "Lucy Nguyen", "0421 222 333", dietitian, LocalTime.of(10, 0));
        book(manager, "Peter Novak", "0466 777 888", surgeon, LocalTime.of(11, 30));
        book(manager, "Sam Patel", "0400 555 111", gpOkafor, LocalTime.of(13, 30));

        // Sorting is inherited from Appointment: the manager returns a sorted copy.
        showAppointments("All appointments, sorted from earliest to latest", manager.getAllAppointments());

        subheading("PART 5: FILTERING AND SORTING");
        showAppointments("Appointments for professional ID 1001",
                manager.getAppointmentsForProfessional("1001"));
        showAppointments("Appointments for patient 0433 111 222 (typed with spaces)",
                manager.getAppointmentsForPatient("0433 111 222"));
        System.out.println();
        System.out.println("  Free slots left for " + gpChen.getName() + ": "
                + times(manager.getAvailableSlots(gpChen)));
        System.out.println("  Free slots left for " + gpMartin.getName() + ": "
                + times(manager.getAvailableSlots(gpMartin)));

        subheading("PARTS 5-6: BOOKINGS THE CLINIC MUST REFUSE");
        // Same professional, same slot -> double booking.
        book(manager, "Nina Rossi", "0499 123 456", gpChen, LocalTime.of(9, 0));
        // Not rostered today.
        book(manager, "Nina Rossi", "0499 123 456", nurseOffDuty, LocalTime.of(10, 30));
        // Malformed patient data never reaches the schedule.
        book(manager, "Nina Rossi", "not-a-phone", cardiologist, LocalTime.of(11, 0));
        // 09:15 and 07:30 are not clinic slots.
        book(manager, "Nina Rossi", "0499 123 456", gpChen, LocalTime.of(9, 15));
        book(manager, "Nina Rossi", "0499 123 456", gpChen, LocalTime.of(7, 30));
        // Appointments cannot be made for any other day.
        System.out.println("  " + manager.book("Nina Rossi", "0499 123 456", gpChen,
                today.plusDays(1), LocalTime.of(9, 0)));

        subheading("PARTS 5-6: CANCELLATIONS");
        System.out.println("  " + manager.cancel("1001", LocalTime.of(9, 30)));
        System.out.println("  " + manager.cancel("9999", LocalTime.of(10, 0)));
        System.out.println("  " + manager.cancelAllForPatient("0433 111 222"));
        System.out.println("  " + manager.cancelAllForPatient("0000 000 000"));

        showAppointments("All appointments after the cancellations", manager.getAllAppointments());
        System.out.println();
        System.out.println("  The schedule now holds " + plural(manager.size(), "appointment") + ".");
        System.out.println(RULE);
    }

    // -------------------------------------------------------------------------
    // Demo data helper
    // -------------------------------------------------------------------------

    /**
     * Builds a weekly roster from the preferred weekdays, always including the
     * day the demo runs on.
     *
     * <p>The demo cannot hard-code "Monday, Wednesday, Friday" alone: the clinic
     * only accepts appointments for <em>today</em>, so on a Saturday nobody would
     * be on duty and the script would demonstrate nothing but failures. Adding
     * today's weekday keeps the sample staff realistic (a normal-looking weekly
     * roster) while guaranteeing that the demonstration works on any day it is
     * run.</p>
     *
     * @param preferredDays the weekdays the professional normally works
     * @return a roster that contains those days and today
     */
    private static Set<DayOfWeek> roster(DayOfWeek... preferredDays) {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        days.addAll(Arrays.asList(preferredDays));
        days.add(LocalDate.now().getDayOfWeek());
        return days;
    }

    // -------------------------------------------------------------------------
    // Output helpers - keeping them here means main() reads like a scenario
    // -------------------------------------------------------------------------

    /**
     * Books an appointment and prints the manager's verdict.
     *
     * @param manager      the schedule to book into
     * @param patientName  the patient's name
     * @param patientPhone the patient's phone number
     * @param professional the professional to see
     * @param time         the start time
     */
    private static void book(AppointmentManager manager, String patientName, String patientPhone,
                             HealthProfessional professional, LocalTime time) {
        System.out.println("  " + manager.book(patientName, patientPhone, professional, time));
    }

    /**
     * Prints a titled list of appointments.
     *
     * @param title        the heading to print
     * @param appointments the appointments to show
     */
    private static void showAppointments(String title, List<Appointment> appointments) {
        System.out.println();
        System.out.println("  " + title + " -> " + plural(appointments.size(), "appointment"));
        if (appointments.isEmpty()) {
            System.out.println("    (none)");
            return;
        }
        for (Appointment appointment : appointments) {
            System.out.println("    " + appointment);
        }
    }

    /**
     * Renders a list of times as {@code 09:30, 10:00, 10:30} for compact
     * printing.
     *
     * @param slots the times to render
     * @return the times as text, or {@code "(none)"} when the list is empty
     */
    private static String times(List<LocalTime> slots) {
        if (slots.isEmpty()) {
            return "(none)";
        }
        StringBuilder text = new StringBuilder();
        for (LocalTime slot : slots) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(slot.format(TIME));
        }
        return text.toString();
    }

    /**
     * Formats a count together with its noun, using the singular form for one.
     *
     * @param count the number of items
     * @param noun  the noun in its singular form, e.g. {@code "appointment"}
     * @return text such as {@code "1 appointment"} or {@code "3 appointments"}
     */
    private static String plural(int count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }

    /**
     * Prints a section banner.
     *
     * @param title the section title
     */
    private static void heading(String title) {
        System.out.println(RULE);
        System.out.println(title);
        System.out.println(RULE);
    }

    /**
     * Prints a sub-section banner.
     *
     * @param title the sub-section title
     */
    private static void subheading(String title) {
        System.out.println();
        System.out.println("-- " + title + " " + repeat('-', Math.max(0, 74 - title.length())));
    }

    /**
     * @param character the character to repeat
     * @param count     how many times to repeat it
     * @return a string of {@code count} copies of {@code character}
     */
    private static String repeat(char character, int count) {
        StringBuilder text = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            text.append(character);
        }
        return text.toString();
    }
}
