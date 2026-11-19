package clinic.booking;

import clinic.Validation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The timetable the clinic runs on: when it opens, when it closes and how long
 * one appointment slot lasts.
 *
 * <p><b>Why a separate class.</b> The fixed set of slots is a <em>policy</em>,
 * not a property of a single appointment, and it must be shared by every booking
 * decision. Keeping it in one immutable object means the rest of the system never
 * hard-codes {@code 08:00}, {@code 16:00} or {@code 30} minutes: the slots are
 * generated from the three values, so changing the clinic to 15 minute slots or
 * a 09:00 start is a one-line change in one place, and every validation,
 * listing and free-slot search follows automatically.</p>
 *
 * <p>The generated slot list is computed once in the constructor and exposed as
 * an unmodifiable list, so it is cheap to query and impossible to corrupt.</p>
 */
public final class ClinicHours {

    /** The clinic opens at 08:00 by default. */
    public static final LocalTime DEFAULT_OPENING = LocalTime.of(8, 0);

    /** The clinic closes at 16:00 by default. */
    public static final LocalTime DEFAULT_CLOSING = LocalTime.of(16, 0);

    /** One appointment (and one patient) per 30 minutes by default. */
    public static final int DEFAULT_SLOT_MINUTES = 30;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalTime openingTime;
    private final LocalTime closingTime;
    private final int slotMinutes;
    private final List<LocalTime> slots;

    /**
     * Creates the clinic's standard timetable: 08:00-16:00 in 30 minute slots.
     */
    public ClinicHours() {
        this(DEFAULT_OPENING, DEFAULT_CLOSING, DEFAULT_SLOT_MINUTES);
    }

    /**
     * Creates a custom timetable. The closing time itself is not a slot: the
     * last bookable slot starts one slot length before closing.
     *
     * @param openingTime the first bookable start time
     * @param closingTime the time the clinic closes
     * @param slotMinutes the length of one slot, in minutes
     * @throws IllegalArgumentException if the times are missing, the closing
     *                                  time is not after the opening time, or
     *                                  the slot length is not positive
     */
    public ClinicHours(LocalTime openingTime, LocalTime closingTime, int slotMinutes) {
        this.openingTime = Validation.required(openingTime, "Opening time");
        this.closingTime = Validation.required(closingTime, "Closing time");
        if (!openingTime.isBefore(closingTime)) {
            throw new IllegalArgumentException("The clinic must close after it opens.");
        }
        if (slotMinutes <= 0) {
            throw new IllegalArgumentException("A slot must be at least one minute long.");
        }
        this.slotMinutes = slotMinutes;
        this.slots = Collections.unmodifiableList(buildSlots());
    }

    /**
     * Generates every start time from the opening time up to (but not
     * including) the closing time, stepping by one slot length.
     *
     * @return the list of bookable start times
     */
    private List<LocalTime> buildSlots() {
        List<LocalTime> result = new ArrayList<>();
        for (LocalTime slot = openingTime; slot.isBefore(closingTime); slot = slot.plusMinutes(slotMinutes)) {
            result.add(slot);
        }
        return result;
    }

    /**
     * @return the first bookable start time
     */
    public LocalTime getOpeningTime() {
        return openingTime;
    }

    /**
     * @return the time the clinic closes
     */
    public LocalTime getClosingTime() {
        return closingTime;
    }

    /**
     * @return the length of one slot in minutes
     */
    public int getSlotMinutes() {
        return slotMinutes;
    }

    /**
     * @return every bookable start time, in chronological order
     */
    public List<LocalTime> getSlots() {
        return slots;
    }

    /**
     * Decides whether a time is the start of a real slot. Times such as 09:15 or
     * 07:30 are not slots and must be rejected, not silently rounded.
     *
     * @param time the time to test, possibly {@code null}
     * @return {@code true} if the time starts a slot
     */
    public boolean isSlot(LocalTime time) {
        return time != null && slots.contains(time);
    }

    /**
     * @return a readable summary, e.g. {@code "08:00-16:00, 30 minute slots (16 slots per day)"}
     */
    public String describe() {
        return String.format("%s-%s, %d minute slots (%d slots per day)",
                openingTime.format(TIME_FORMAT), closingTime.format(TIME_FORMAT), slotMinutes, slots.size());
    }

    @Override
    public String toString() {
        return describe();
    }
}
