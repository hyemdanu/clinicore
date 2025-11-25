package com.clinicore.project.util;

import java.time.LocalDateTime;


/**
 * utility for the scheduling and timing of the medications
 * kinda like handles the logic for medication schedules
 * like seeing when the next dose is due based on the schedule string
 * and if it's overdue
 */
public class MedicationScheduleUtil {

    public static Integer getHoursFromSchedule(String schedule) {
        if (schedule == null) return null;

        String lower = schedule.toLowerCase().trim();

        if (lower.contains("once") && lower.contains("daily")) return 24;
        if (lower.contains("twice") && lower.contains("daily")) return 12;
        if (lower.contains("three") && lower.contains("daily")) return 8;
        if (lower.contains("four") && lower.contains("daily")) return 6;

        if (lower.contains("every 4 hours")) return 4;
        if (lower.contains("every 6 hours")) return 6;
        if (lower.contains("every 8 hours")) return 8;
        if (lower.contains("every 12 hours")) return 12;

        if (lower.contains("morning only")) return 24;
        if (lower.contains("evening only")) return 24;
        if (lower.contains("bedtime")) return 24;
        if (lower.contains("before meals")) return 8; // 3 meals
        if (lower.contains("after meals")) return 8;

        if (lower.contains("as needed") || lower.contains("prn")) return null;

        return null;
    }

    public static LocalDateTime calculateNextDoseTime(LocalDateTime lastAdministered, String schedule) {
        if (lastAdministered == null) return null;

        Integer hours = getHoursFromSchedule(schedule);
        if (hours == null) return null; // PRN or unknown

        return lastAdministered.plusHours(hours);
    }

    public static boolean isOverdue(LocalDateTime nextDoseTime) {
        if (nextDoseTime == null) return false;

        LocalDateTime twoHoursAfterDue = nextDoseTime.plusHours(2);
        return LocalDateTime.now().isAfter(twoHoursAfterDue);
    }
}
