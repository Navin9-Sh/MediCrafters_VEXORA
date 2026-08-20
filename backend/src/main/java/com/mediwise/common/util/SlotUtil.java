package com.mediwise.common.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for generating time slots from doctor schedule templates.
 */
@Component
public class SlotUtil {

    /**
     * Generate a list of LocalTime slot start times for a given schedule window.
     *
     * @param startTime      window start (e.g. 09:00)
     * @param endTime        window end (e.g. 17:00)
     * @param durationMins   slot duration in minutes (e.g. 30)
     * @return list of slot start times within the window
     */
    public static List<LocalTime> generateSlotTimes(LocalTime startTime, LocalTime endTime, int durationMins) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = startTime;
        while (current.plusMinutes(durationMins).compareTo(endTime) <= 0) {
            slots.add(current);
            current = current.plusMinutes(durationMins);
        }
        return slots;
    }

    /**
     * Returns the next N dates (from today inclusive) that match the given day-of-week.
     * DayOfWeek: MONDAY=1 ... SUNDAY=7 (java.time convention).
     */
    public static List<LocalDate> nextDatesForDayOfWeek(DayOfWeek dayOfWeek, int count) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = LocalDate.now();
        while (dates.size() < count) {
            if (date.getDayOfWeek() == dayOfWeek) {
                dates.add(date);
            }
            date = date.plusDays(1);
        }
        return dates;
    }

    /**
     * Checks if a proposed slot [slotStart, slotStart+duration) overlaps
     * with any existing booked slot in the provided list.
     */
    public static boolean hasOverlap(LocalTime slotStart, int durationMins, List<LocalTime> existingStarts) {
        LocalTime slotEnd = slotStart.plusMinutes(durationMins);
        for (LocalTime existing : existingStarts) {
            LocalTime existingEnd = existing.plusMinutes(durationMins);
            if (slotStart.isBefore(existingEnd) && slotEnd.isAfter(existing)) {
                return true;
            }
        }
        return false;
    }
}
