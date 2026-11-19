package clinic.staff;

import clinic.Validation;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * The state and behaviour shared by <em>every</em> health professional who works
 * at the clinic, whatever their role.
 *
 * <p><b>Why this class is abstract.</b> The clinic never employs a "plain"
 * health professional, so the design forbids creating one: {@code new
 * HealthProfessional(...)} does not compile. Every object must belong to a
 * concrete role that states what it is ({@link #getRole()}) and what makes it
 * different ({@link #describeSpecialisation()}). This turns a modelling mistake
 * into a compile error instead of a strange-looking record in the system.</p>
 *
 * <p><b>Why the fields are final and private.</b> The id, the name and the
 * weekly roster are facts about a person that do not change inside this model.
 * They are validated once in the constructor and then only exposed through
 * getters, so no outside code can push an instance into an illegal state (for
 * example an empty roster). The incoming set of working days is copied
 * defensively, so keeping a reference to the caller's set cannot be used to
 * modify us afterwards.</p>
 *
 * <p><b>Printing.</b> {@link #toString()} is implemented once, here, using the
 * <em>template method</em> pattern: the class builds the common part of the
 * description and each subclass fills in the detail text through
 * {@link #describeSpecialisation()}. A subclass therefore cannot forget to
 * include the shared information.</p>
 *
 * <p><b>Equality.</b> Two professionals are the same person when they carry the
 * same id. {@link #equals(Object)} and {@link #hashCode()} are both overridden
 * so instances behave correctly in sets, maps and {@code contains} checks -
 * this is what {@link AppointmentManager} relies on when it decides whether two
 * appointments belong to the same professional.</p>
 */
public abstract class HealthProfessional {

    /** Monday to Friday; the roster most full-time staff at the clinic work. */
    protected static final Set<DayOfWeek> STANDARD_WEEK =
            Collections.unmodifiableSet(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));

    private final String id;
    private final String name;
    private final Set<DayOfWeek> workingDays;

    /**
     * Creates a professional with an explicit weekly roster.
     *
     * @param id          numeric identifier (digits only), e.g. {@code "1001"}
     * @param name        full name, must not be blank
     * @param workingDays the days this person is in the clinic; at least one is
     *                    required
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    protected HealthProfessional(String id, String name, Set<DayOfWeek> workingDays) {
        this.id = Validation.digitsOnly(id, "Professional id");
        this.name = Validation.text(name, "Professional name");
        this.workingDays = immutableCopyOf(workingDays);
    }

    /**
     * Convenience constructor for staff who work the standard Monday to Friday
     * week. Offering a second way to build an object is worthwhile here because
     * most full-time staff share the same roster: callers who have no special
     * information to pass do not have to invent it, while callers who do (a
     * part-time nurse, for instance) keep full control.
     *
     * @param id   numeric identifier (digits only)
     * @param name full name, must not be blank
     */
    protected HealthProfessional(String id, String name) {
        this(id, name, STANDARD_WEEK);
    }

    /**
     * Copies the supplied roster into an unmodifiable {@link EnumSet}.
     *
     * <p>{@code EnumSet} is the right collection for "a fixed set of weekdays":
     * it only accepts the enum's constants, it always iterates in calendar
     * order (so printing a roster needs no sorting), and it is extremely cheap.
     * The unmodifiable wrapper keeps the collection read-only from outside.</p>
     *
     * @param days the roster supplied by the caller
     * @return an unmodifiable copy of the roster
     */
    private static Set<DayOfWeek> immutableCopyOf(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("A health professional must work on at least one day of the week.");
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(days));
    }

    /**
     * @return the numeric identifier of this professional
     */
    public String getId() {
        return id;
    }

    /**
     * @return the full name of this professional
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the roster as a read-only set. Because the set is immutable, the
     * caller can inspect it freely without being able to change this object.
     *
     * @return the days this professional works, in calendar order
     */
    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    /**
     * @param day the day to test
     * @return {@code true} if this professional is rostered on that day
     */
    public boolean worksOn(DayOfWeek day) {
        return workingDays.contains(day);
    }

    /**
     * Renders the roster as a short, readable list such as
     * {@code "Mon, Wed, Fri"}.
     *
     * @return the roster as text
     */
    public String getWorkingDaysAsText() {
        StringBuilder text = new StringBuilder();
        // EnumSet iterates in the enum's natural (calendar) order.
        for (DayOfWeek day : workingDays) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        return text.toString();
    }

    /**
     * @return the human readable role of this professional, e.g.
     *         {@code "General Practitioner"}
     */
    public abstract String getRole();

    /**
     * Describes the information and abilities that are specific to this role.
     * Called by {@link #toString()}, so a subclass never has to repeat the
     * common details.
     *
     * @return a one-line summary of the role-specific details
     */
    protected abstract String describeSpecialisation();

    /**
     * Produces a single line that shows every detail of this professional: the
     * role, the id, the name, the weekly roster and the role-specific
     * information. Printing an object with {@code System.out.println(staff)}
     * therefore needs no extra code at the call site.
     *
     * @return a readable description of this professional
     */
    @Override
    public String toString() {
        return String.format("[%s] ID %s | %s | works %s | %s",
                getRole(), getId(), getName(), getWorkingDaysAsText(), describeSpecialisation());
    }

    /**
     * Two professionals are the same person exactly when they share an id. The
     * check is written against the base type on purpose, so a specialist and a
     * nurse with the same id would still be treated as the same staff member.
     *
     * @param other the object to compare with
     * @return {@code true} if {@code other} is a professional with the same id
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HealthProfessional)) {
            return false;
        }
        HealthProfessional that = (HealthProfessional) other;
        return id.equals(that.id);
    }

    /**
     * @return a hash consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
